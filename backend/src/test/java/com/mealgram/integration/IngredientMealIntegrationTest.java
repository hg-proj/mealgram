package com.mealgram.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;

@EnabledIf("com.mealgram.integration.IntegrationTestSupport#dockerAvailable")
class IngredientMealIntegrationTest extends IntegrationTestSupport {

    private static final String MEAL_PLAN = """
            {"meals":[{"index":0,"style":"스타일0"},
                      {"index":1,"style":"스타일1"},
                      {"index":2,"style":"스타일2"}]}""";

    @BeforeEach
    void stubExternalCalls() {

        when(llmClient.chat(anyString(), anyString())).thenReturn(MEAL_PLAN);

    }

    private int status(MvcResult result) {

        return result.getResponse().getStatus();

    }

    private long ingredientId(String keyword, String token) throws Exception {

        return body(getJson("/api/ingredients?keyword=" + keyword, token)).get(0).get("id").asLong();

    }

    @Test
    @DisplayName("재료 검색은 이름순으로 나오고 한 번에 100개까지만 가져오며 허용하지 않는 정렬은 400이다.")
    void ingredientSearch() throws Exception {

        Member member = signupAndLogin("srch");

        JsonNode tofu = body(getJson("/api/ingredients?keyword=두부", member.accessToken()));
        assertTrue(tofu.size() > 0);
        List<String> names = new ArrayList<>();
        tofu.forEach(item -> names.add(item.get("name").asString()));
        assertEquals(names.stream().sorted().toList(), names);

        assertEquals(100, body(getJson("/api/ingredients?keyword=&size=500", member.accessToken())).size());
        assertEquals(400, status(getJson("/api/ingredients?sort=password,desc", member.accessToken())));

        for (JsonNode item : body(getJson("/api/ingredients?keyword=&size=100", member.accessToken()))) {
            assertFalse(item.get("name").asString().isBlank());
        }

    }

    @Test
    @DisplayName("내 재료는 최근 등록한 것이 먼저 나오고 같은 재료를 두 번 등록하면 409다.")
    void myIngredients() throws Exception {

        Member member = signupAndLogin("mine");
        long tofu = ingredientId("두부", member.accessToken());
        long pork = ingredientId("돼지고기", member.accessToken());

        MvcResult first = postJson("/api/members/me/ingredients", Map.of("ingredientId", tofu), member.accessToken());
        assertEquals(201, status(first));
        assertEquals(201, status(postJson("/api/members/me/ingredients", Map.of("ingredientId", pork), member.accessToken())));
        assertEquals(409, status(postJson("/api/members/me/ingredients", Map.of("ingredientId", tofu), member.accessToken())));
        assertEquals(404, status(postJson("/api/members/me/ingredients", Map.of("ingredientId", 999999999L), member.accessToken())));

        JsonNode list = body(getJson("/api/members/me/ingredients", member.accessToken()));
        assertEquals(2, list.size());
        assertEquals(pork, list.get(0).get("ingredientId").asLong());
        assertEquals(tofu, list.get(1).get("ingredientId").asLong());
        assertEquals(204, status(deleteJson("/api/members/me/ingredients/" + body(first).get("id").asLong(), member.accessToken())));
        assertEquals(1, body(getJson("/api/members/me/ingredients", member.accessToken())).size());

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
                futures.add(executor.submit(() -> status(postJson("/api/members/me/ingredients", Map.of("ingredientId", ingredient), member.accessToken()))));
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
    @DisplayName("저장식단은 추천받은 식단 한 상을 저장, 중복 방지, 최신순 목록, 삭제가 되고 다른 회원의 것은 건드릴 수 없다.")
    void savedMeals() throws Exception {

        Member owner = signupAndLogin("sv");
        Member other = signupAndLogin("so");
        String mealId = body(postJson("/api/meals", Map.of("goal", "유지"), owner.accessToken())).get("id").asString();

        MvcResult first = postJson("/api/saved-meals", Map.of("mealId", mealId, "candidateIndex", 0), owner.accessToken());
        MvcResult second = postJson("/api/saved-meals", Map.of("mealId", mealId, "candidateIndex", 1), owner.accessToken());
        assertEquals(201, status(first));
        assertEquals(201, status(second));
        assertEquals(409, status(postJson("/api/saved-meals", Map.of("mealId", mealId, "candidateIndex", 0), owner.accessToken())));
        assertEquals(404, status(postJson("/api/saved-meals", Map.of("mealId", mealId, "candidateIndex", 0), other.accessToken())));
        assertEquals(404, status(postJson("/api/saved-meals",
                Map.of("mealId", "00000000-0000-0000-0000-000000000000", "candidateIndex", 0), owner.accessToken())));
        assertEquals(400, status(postJson("/api/saved-meals", Map.of("mealId", mealId, "candidateIndex", 3), owner.accessToken())));
        assertEquals(400, status(postJson("/api/saved-meals", Map.of(), owner.accessToken())));

        JsonNode list = body(getJson("/api/saved-meals", owner.accessToken()));
        assertEquals(2, list.size());
        assertEquals(body(second).get("id").asLong(), list.get(0).get("id").asLong());
        assertFalse(list.get(0).get("style").asString().isBlank());
        assertTrue(list.get(0).get("dishes").size() >= 1);
        assertTrue(list.get(0).get("nutrition").get("targetCalorie").asInt() > 0);
        assertEquals(0, body(getJson("/api/saved-meals", other.accessToken())).size());
        assertEquals(400, status(getJson("/api/saved-meals?sort=dishesJson", owner.accessToken())));

        long firstId = body(first).get("id").asLong();
        assertEquals(404, status(deleteJson("/api/saved-meals/" + firstId, other.accessToken())));
        assertEquals(204, status(deleteJson("/api/saved-meals/" + firstId, owner.accessToken())));
        assertEquals(1, body(getJson("/api/saved-meals", owner.accessToken())).size());

    }

    @Test
    @DisplayName("식단 추천은 요리를 조합해 3개를 주고 만든 회원만 다시 볼 수 있다.")
    void mealRecommendation() throws Exception {

        Member member = signupAndLogin("meal");
        Member other = signupAndLogin("mealo");

        MvcResult created = postJson("/api/meals", Map.of("goal", "다이어트", "genre", "한식"), member.accessToken());
        assertEquals(201, status(created));
        JsonNode meals = body(created);
        assertFalse(meals.get("relaxed").asBoolean());
        assertEquals(3, meals.get("candidates").size());
        for (JsonNode candidate : meals.get("candidates")) {
            assertTrue(candidate.get("dishes").size() >= 1);
            JsonNode nutrition = candidate.get("nutrition");
            assertTrue(nutrition.get("targetCalorie").asInt() > 0);
            assertTrue(nutrition.has("verified"));
        }

        String id = meals.get("id").asString();
        assertEquals(200, status(getJson("/api/meals/" + id, member.accessToken())));
        assertEquals(id, body(getJson("/api/meals/" + id, member.accessToken())).get("id").asString());
        assertEquals(404, status(getJson("/api/meals/" + id, other.accessToken())));
        assertEquals(404, status(getJson("/api/meals/00000000-0000-0000-0000-000000000000", member.accessToken())));
        assertEquals(400, status(postJson("/api/meals", Map.of("goal", "아무거나"), member.accessToken())));

    }

    @Test
    @DisplayName("식단 추천은 회원당 호출 횟수를 넘으면 429와 Retry-After로 막는다.")
    void mealRateLimit() throws Exception {

        Member member = signupAndLogin("rate");
        Member other = signupAndLogin("rateo");

        for (int i = 0; i < 3; i++) {
            assertEquals(201, status(postJson("/api/meals", Map.of("goal", "유지"), member.accessToken())));
        }
        MvcResult blocked = postJson("/api/meals", Map.of("goal", "유지"), member.accessToken());

        assertEquals(429, status(blocked));
        assertEquals("RATE_LIMIT_EXCEEDED", body(blocked).get("error").get("code").asString());
        assertTrue(Integer.parseInt(blocked.getResponse().getHeader("Retry-After")) > 0);
        assertEquals(200, status(getJson("/api/members/me", member.accessToken())));
        assertEquals(201, status(postJson("/api/meals", Map.of("goal", "유지"), other.accessToken())));

    }

    @Test
    @DisplayName("존재하지 않는 회원의 추천 요청은 외부 호출 없이 404다.")
    void mealRecommendationForDeletedMember() throws Exception {

        Member member = signupAndLogin("gone");
        assertEquals(204, status(deleteJson("/api/members/me", member.accessToken())));

        MvcResult result = postJson("/api/meals", Map.of("goal", "유지"), member.accessToken());

        assertEquals(404, status(result));
        assertEquals("MEMBER_NOT_FOUND", body(result).get("error").get("code").asString());

    }

    @Test
    @DisplayName("선택할 수 있는 재료는 요리의 재료 목록에 있는 재료만이고 양념은 나오지 않는다.")
    void catalogHasOnlyDishIngredients() throws Exception {

        JsonNode catalog = body(getJson("/api/ingredients/catalog", null));
        List<String> names = new ArrayList<>();
        catalog.forEach(category -> category.get("ingredients").forEach(item -> names.add(item.get("name").asString())));

        assertTrue(names.contains("양파"));
        assertTrue(names.contains("돼지고기"));
        assertFalse(names.contains("소금"));
        assertFalse(names.contains("간장"));

    }

}
