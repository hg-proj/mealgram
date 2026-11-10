package com.mealgram.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;

@EnabledIf("com.mealgram.integration.IntegrationTestSupport#dockerAvailable")
class GuestMealIntegrationTest extends IntegrationTestSupport {

    private static final Pattern RECIPE_LINE = Pattern.compile("id=(\\d+) \\| [^|]+ \\| ([^|]+) \\|");
    private static final Map<String, Object> BODY = Map.of("goal", "유지", "birthDate", birthDateYearsAgo(30), "gender", "MALE", "height", 175,
            "weight", 70, "activityLevel", "MODERATE");

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

    private MvcResult guestRecommend(String address, Object body) throws Exception {

        return mockMvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content(json(body))
                .with(request -> {
                    request.setRemoteAddr(address);
                    return request;
                })).andReturn();

    }

    @Test
    @DisplayName("로그인하지 않아도 재료를 검색할 수 있다.")
    void guestSearchesIngredients() throws Exception {

        MvcResult result = getJson("/api/ingredients?keyword=두부", null);

        assertEquals(200, result.getResponse().getStatus());
        assertTrue(body(result).size() > 0);

    }

    @Test
    @DisplayName("로그인하지 않아도 신체 정보를 보내 식단 세 개를 추천받고 결과 id는 비어 있다.")
    void guestRecommends() throws Exception {

        MvcResult result = guestRecommend("10.1.0.1", BODY);
        JsonNode response = body(result);

        assertEquals(201, result.getResponse().getStatus());
        assertEquals(3, response.get("candidates").size());
        assertTrue(response.get("id").isNull());
        assertNotNull(response.get("candidates").get(0).get("nutrition").get("targetCalorie"));

    }

    @Test
    @DisplayName("신체 정보 없이도 추천받고 범위를 벗어난 값은 400이다.")
    void guestBodyInfoIsOptionalAndValidated() throws Exception {

        assertEquals(201, guestRecommend("10.1.0.2", Map.of("goal", "유지")).getResponse().getStatus());

        MvcResult invalid = guestRecommend("10.1.0.2", Map.of("goal", "유지", "birthDate", birthDateYearsAgo(2)));

        assertEquals(400, invalid.getResponse().getStatus());
        assertEquals("VALIDATION_FAILED", body(invalid).get("error").get("code").asString());

    }

    @Test
    @DisplayName("로그인하지 않은 요청은 접속 주소별로 제한하고 다른 주소는 막지 않는다.")
    void guestIsRateLimitedByAddress() throws Exception {

        assertEquals(201, guestRecommend("10.1.0.3", BODY).getResponse().getStatus());
        assertEquals(201, guestRecommend("10.1.0.3", BODY).getResponse().getStatus());

        MvcResult blocked = guestRecommend("10.1.0.3", BODY);

        assertEquals(429, blocked.getResponse().getStatus());
        assertNotNull(blocked.getResponse().getHeader("Retry-After"));
        assertEquals(201, guestRecommend("10.1.0.4", BODY).getResponse().getStatus());

    }

    @Test
    @DisplayName("비회원은 추천 결과 다시 보기와 회원 전용 기능을 쓸 수 없다.")
    void guestCannotUseMemberFeatures() throws Exception {

        assertEquals(401, getJson("/api/meals/00000000-0000-0000-0000-000000000000", null).getResponse().getStatus());
        assertEquals(401, getJson("/api/members/me", null).getResponse().getStatus());
        assertEquals(401, getJson("/api/members/me/ingredients", null).getResponse().getStatus());
        assertEquals(401, getJson("/api/saved-meals", null).getResponse().getStatus());
        assertEquals(401, postJson("/api/saved-meals", Map.of("recipeId", 1), null).getResponse().getStatus());

    }

    @Test
    @DisplayName("비회원은 보낸 재료 번호로 레시피 부족 재료를 계산한다.")
    void guestRecipeDetailUsesGivenIngredients() throws Exception {

        JsonNode meal = body(guestRecommend("10.1.0.5", BODY));
        long recipeId = meal.get("candidates").get(0).get("recipes").get(0).get("id").asLong();

        JsonNode all = body(getJson("/api/recipes/" + recipeId, null));
        long tofu = body(getJson("/api/ingredients?keyword=두부", null)).get(0).get("id").asLong();
        JsonNode withIngredient = body(getJson("/api/recipes/" + recipeId + "?ingredientIds=" + tofu, null));

        assertTrue(all.get("missingIngredients").isArray());
        assertTrue(withIngredient.get("missingIngredients").size() <= all.get("missingIngredients").size());

    }

}
