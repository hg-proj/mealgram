package com.mealgram.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;

@EnabledIf("com.mealgram.integration.IntegrationTestSupport#dockerAvailable")
class IngredientMealIntegrationTest extends IntegrationTestSupport {

    private static final Pattern RECIPE_LINE = Pattern.compile("id=(\\d+) \\| [^|]+ \\| ([^|]+) \\|");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void stubExternalCalls() {

        float[] vector = new float[1536];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = 0.01f + (i % 7) * 0.001f;
        }
        when(embeddingClient.embed(anyList())).thenAnswer(invocation -> List.of(vector));
        when(llmClient.chat(anyString(), anyString())).thenAnswer(invocation -> mealPlanFor(invocation.getArgument(1)));

    }

    private String mealPlanFor(String prompt) {

        Map<String, List<Long>> byCategory = new LinkedHashMap<>();
        Matcher matcher = RECIPE_LINE.matcher(prompt);
        while (matcher.find()) {
            byCategory.computeIfAbsent(matcher.group(2).trim(), key -> new ArrayList<>()).add(Long.parseLong(matcher.group(1)));
        }
        List<String> meals = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<Long> ids = new ArrayList<>();
            addAt(ids, byCategory.get("밥"), i, 1);
            addAt(ids, byCategory.get("국&찌개"), i, 1);
            addAt(ids, byCategory.get("반찬"), i * 3, 3);
            meals.add("{\"style\":\"스타일" + i + "\",\"reason\":\"테스트 설명\",\"recipeIds\":" + ids + "}");
        }

        return "{\"meals\":[" + String.join(",", meals) + "]}";

    }

    private void addAt(List<Long> ids, List<Long> source, int from, int count) {

        if (source == null) {
            return;
        }
        for (int i = 0; i < count && from + i < source.size(); i++) {
            ids.add(source.get(from + i));
        }

    }

    private int status(MvcResult result) {

        return result.getResponse().getStatus();

    }

    private long ingredientId(String keyword, String token) throws Exception {

        return body(getJson("/ingredients?keyword=" + keyword, token)).get(0).get("id").asLong();

    }

    @Test
    @DisplayName("재료 검색은 이름순으로 나오고 한 번에 100개까지만 가져오며 허용하지 않는 정렬은 400이다.")
    void ingredientSearch() throws Exception {

        Member member = signupAndLogin("srch");

        JsonNode tofu = body(getJson("/ingredients?keyword=두부", member.accessToken()));
        assertTrue(tofu.size() > 0);
        List<String> names = new ArrayList<>();
        tofu.forEach(item -> names.add(item.get("name").asString()));
        assertEquals(names.stream().sorted().toList(), names);

        assertEquals(100, body(getJson("/ingredients?keyword=&size=500", member.accessToken())).size());
        assertEquals(400, status(getJson("/ingredients?sort=password,desc", member.accessToken())));

        for (JsonNode item : body(getJson("/ingredients?keyword=&size=100", member.accessToken()))) {
            assertFalse(item.get("name").asString().isBlank());
        }

    }

    @Test
    @DisplayName("내 재료는 최근 등록한 것이 먼저 나오고 같은 재료를 두 번 등록하면 409다.")
    void myIngredients() throws Exception {

        Member member = signupAndLogin("mine");
        long tofu = ingredientId("두부", member.accessToken());
        long pork = ingredientId("돼지고기", member.accessToken());

        MvcResult first = postJson("/members/me/ingredients", Map.of("ingredientId", tofu), member.accessToken());
        assertEquals(201, status(first));
        assertEquals(201, status(postJson("/members/me/ingredients", Map.of("ingredientId", pork), member.accessToken())));
        assertEquals(409, status(postJson("/members/me/ingredients", Map.of("ingredientId", tofu), member.accessToken())));
        assertEquals(404, status(postJson("/members/me/ingredients", Map.of("ingredientId", 999999999L), member.accessToken())));

        JsonNode list = body(getJson("/members/me/ingredients", member.accessToken()));
        assertEquals(2, list.size());
        assertEquals(pork, list.get(0).get("ingredientId").asLong());
        assertEquals(tofu, list.get(1).get("ingredientId").asLong());
        assertEquals(204, status(deleteJson("/members/me/ingredients/" + body(first).get("id").asLong(), member.accessToken())));
        assertEquals(1, body(getJson("/members/me/ingredients", member.accessToken())).size());

    }

    @Test
    @DisplayName("같은 재료를 동시에 등록해도 하나만 성공하고 나머지는 409이며 500은 없다.")
    void concurrentRegistration() throws Exception {

        Member member = signupAndLogin("conc");
        long ingredient = ingredientId("김치", member.accessToken());

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(() -> status(postJson("/members/me/ingredients", Map.of("ingredientId", ingredient), member.accessToken()))));
            }
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statuses.add(future.get());
            }

            assertEquals(1, statuses.stream().filter(code -> code == 201).count(), statuses.toString());
            assertEquals(7, statuses.stream().filter(code -> code == 409).count(), statuses.toString());
        } finally {
            executor.shutdown();
        }

    }

    @Test
    @DisplayName("저장식단은 저장, 중복 방지, 최신순 목록, 삭제가 되고 다른 회원의 것은 건드릴 수 없다.")
    void savedMeals() throws Exception {

        Member owner = signupAndLogin("sv");
        Member other = signupAndLogin("so");
        List<Long> recipeIds = jdbcTemplate.queryForList("select id from recipe order by id limit 2", Long.class);

        MvcResult first = postJson("/saved-meals", Map.of("recipeId", recipeIds.get(0)), owner.accessToken());
        MvcResult second = postJson("/saved-meals", Map.of("recipeId", recipeIds.get(1)), owner.accessToken());
        assertEquals(201, status(first));
        assertEquals(201, status(second));
        assertEquals(409, status(postJson("/saved-meals", Map.of("recipeId", recipeIds.get(0)), owner.accessToken())));
        assertEquals(404, status(postJson("/saved-meals", Map.of("recipeId", 999999999L), owner.accessToken())));
        assertEquals(400, status(postJson("/saved-meals", Map.of(), owner.accessToken())));

        JsonNode list = body(getJson("/saved-meals", owner.accessToken()));
        assertEquals(2, list.size());
        assertEquals(recipeIds.get(1), list.get(0).get("recipeId").asLong());
        assertFalse(list.get(0).get("name").asString().isBlank());
        assertEquals(0, body(getJson("/saved-meals", other.accessToken())).size());
        assertEquals(400, status(getJson("/saved-meals?sort=recipe.name", owner.accessToken())));

        long firstId = body(first).get("id").asLong();
        assertEquals(404, status(deleteJson("/saved-meals/" + firstId, other.accessToken())));
        assertEquals(204, status(deleteJson("/saved-meals/" + firstId, owner.accessToken())));
        assertEquals(1, body(getJson("/saved-meals", owner.accessToken())).size());

    }

    @Test
    @DisplayName("레시피 상세는 내가 가진 재료를 부족 재료에서 빼고 양념은 따로 보여 준다.")
    void recipeDetail() throws Exception {

        Member member = signupAndLogin("rcp");
        Long recipeId = jdbcTemplate.queryForObject("""
                select r.id from recipe r
                join recipe_ingredient ri on ri.recipe_id = r.id
                join ingredient i on i.id = ri.ingredient_id
                group by r.id
                having count(*) filter (where i.category <> '양념') >= 3 and count(*) filter (where i.category = '양념') >= 1
                order by r.id limit 1
                """, Long.class);
        Long ownedIngredient = jdbcTemplate.queryForObject("""
                select i.id from recipe_ingredient ri join ingredient i on i.id = ri.ingredient_id
                where ri.recipe_id = ? and i.category <> '양념' order by i.id limit 1
                """, Long.class, recipeId);
        String ownedName = jdbcTemplate.queryForObject("select name from ingredient where id = ?", String.class, ownedIngredient);

        JsonNode before = body(getJson("/recipes/" + recipeId, member.accessToken()));
        assertTrue(before.toString().contains("\"" + ownedName + "\""));
        assertTrue(before.get("seasonings").size() >= 1);

        assertEquals(201, status(postJson("/members/me/ingredients", Map.of("ingredientId", ownedIngredient), member.accessToken())));
        JsonNode after = body(getJson("/recipes/" + recipeId, member.accessToken()));
        List<String> missing = new ArrayList<>();
        after.get("missingIngredients").forEach(item -> missing.add(item.asString()));

        assertFalse(missing.contains(ownedName));
        assertEquals(before.get("missingIngredients").size() - 1, missing.size());
        assertEquals(before.get("seasonings"), after.get("seasonings"));
        assertEquals(404, status(getJson("/recipes/999999999", member.accessToken())));

    }

    @Test
    @DisplayName("식단 추천은 벡터 검색과 영양 검증을 거쳐 3개를 주고 만든 회원만 다시 볼 수 있다.")
    void mealRecommendation() throws Exception {

        jdbcTemplate.update("""
                update recipe set embedding = (select array_agg((0.01 + (g % 7) * 0.001)::float4)::vector from generate_series(1, 1536) g)
                where id % 3 = 0
                """);
        Member member = signupAndLogin("meal");
        Member other = signupAndLogin("mealo");

        MvcResult created = postJson("/meals", Map.of("goal", "다이어트", "genre", "한식"), member.accessToken());
        assertEquals(201, status(created));
        JsonNode meals = body(created);
        assertFalse(meals.get("relaxed").asBoolean());
        assertEquals(3, meals.get("candidates").size());
        for (JsonNode candidate : meals.get("candidates")) {
            assertTrue(candidate.get("recipes").size() >= 3);
            JsonNode nutrition = candidate.get("nutrition");
            assertTrue(nutrition.get("targetCalorie").asInt() > 0);
            assertTrue(nutrition.has("verified"));
        }

        String id = meals.get("id").asString();
        assertEquals(200, status(getJson("/meals/" + id, member.accessToken())));
        assertEquals(id, body(getJson("/meals/" + id, member.accessToken())).get("id").asString());
        assertEquals(404, status(getJson("/meals/" + id, other.accessToken())));
        assertEquals(404, status(getJson("/meals/00000000-0000-0000-0000-000000000000", member.accessToken())));
        assertEquals(400, status(postJson("/meals", Map.of("goal", "아무거나"), member.accessToken())));

    }

    @Test
    @DisplayName("식단 추천은 회원당 호출 횟수를 넘으면 429와 Retry-After로 막는다.")
    void mealRateLimit() throws Exception {

        Member member = signupAndLogin("rate");
        Member other = signupAndLogin("rateo");

        for (int i = 0; i < 3; i++) {
            assertEquals(201, status(postJson("/meals", Map.of("goal", "유지"), member.accessToken())));
        }
        MvcResult blocked = postJson("/meals", Map.of("goal", "유지"), member.accessToken());

        assertEquals(429, status(blocked));
        assertEquals("RATE_LIMIT_EXCEEDED", body(blocked).get("error").get("code").asString());
        assertTrue(Integer.parseInt(blocked.getResponse().getHeader("Retry-After")) > 0);
        assertEquals(200, status(getJson("/members/me", member.accessToken())));
        assertEquals(201, status(postJson("/meals", Map.of("goal", "유지"), other.accessToken())));

    }

    @Test
    @DisplayName("존재하지 않는 회원의 추천 요청은 외부 호출 없이 404다.")
    void mealRecommendationForDeletedMember() throws Exception {

        Member member = signupAndLogin("gone");
        assertEquals(204, status(deleteJson("/members/me", member.accessToken())));

        MvcResult result = postJson("/meals", Map.of("goal", "유지"), member.accessToken());

        assertEquals(404, status(result));
        assertEquals("MEMBER_NOT_FOUND", body(result).get("error").get("code").asString());

    }

}
