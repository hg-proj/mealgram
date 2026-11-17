package com.mealgram.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.web.servlet.MvcResult;

import com.mealgram.common.exception.ExternalApiException;

import tools.jackson.databind.JsonNode;

@EnabledIf("com.mealgram.integration.IntegrationTestSupport#dockerAvailable")
class ShoppingIntegrationTest extends IntegrationTestSupport {

    @BeforeEach
    void stubLlm() {

        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

    }

    private List<Long> recommendedDishIds(String token) throws Exception {

        JsonNode meal = body(postJson("/api/meals", Map.of("goal", "유지"), token));
        List<Long> ids = new ArrayList<>();
        meal.get("candidates").get(0).get("dishes").forEach(dish -> ids.add(dish.get("id").asLong()));

        return ids;

    }

    private List<String> names(JsonNode items) {

        List<String> names = new ArrayList<>();
        items.forEach(item -> names.add(item.get("name").asString()));

        return names;

    }

    @Test
    @DisplayName("비회원도 식단의 요리 번호로 장보기 목록을 받고 같은 재료는 한 줄로 합쳐진다.")
    void guestGetsShoppingList() throws Exception {

        List<Long> dishIds = recommendedDishIds(null);

        MvcResult result = postJson("/api/shopping-list", Map.of("dishIds", dishIds), null);
        JsonNode list = body(result);

        assertEquals(200, result.getResponse().getStatus());
        assertFalse(list.get("toBuy").isEmpty());
        assertTrue(list.get("have").isEmpty());
        List<String> bought = names(list.get("toBuy"));
        assertEquals(bought.size(), bought.stream().distinct().count());
        for (JsonNode item : list.get("toBuy")) {
            assertFalse(item.get("usedIn").isEmpty());
            assertFalse(item.get("category").asString().isBlank());
        }

    }

    @Test
    @DisplayName("회원이 등록한 내 재료는 사야 할 재료에서 빠지고 가진 재료로 나온다.")
    void memberOwnedIngredientsAreMoved() throws Exception {

        Member member = signupAndLogin("shop");
        List<Long> dishIds = recommendedDishIds(member.accessToken());
        JsonNode before = body(postJson("/api/shopping-list", Map.of("dishIds", dishIds), member.accessToken()));
        String target = before.get("toBuy").get(0).get("name").asString();
        long ingredientId = body(getJson("/api/ingredients?keyword=" + target, member.accessToken())).get(0).get("id").asLong();
        assertEquals(201, postJson("/api/members/me/ingredients", Map.of("ingredientId", ingredientId), member.accessToken())
                .getResponse().getStatus());

        JsonNode after = body(postJson("/api/shopping-list", Map.of("dishIds", dishIds), member.accessToken()));

        assertFalse(names(after.get("toBuy")).contains(target));
        assertTrue(names(after.get("have")).contains(target));
        assertEquals(before.get("toBuy").size() - 1, after.get("toBuy").size());

    }

    @Test
    @DisplayName("요리 번호가 없거나 너무 많거나 없는 번호면 오류다.")
    void validatesRequest() throws Exception {

        assertEquals(400, postJson("/api/shopping-list", Map.of("dishIds", List.of()), null).getResponse().getStatus());
        assertEquals(400, postJson("/api/shopping-list", Map.of(), null).getResponse().getStatus());
        assertEquals(404, postJson("/api/shopping-list", Map.of("dishIds", List.of(999999999L)), null).getResponse().getStatus());
        assertEquals(404, postJson("/api/shopping-list",
                Map.of("dishIds", recommendedDishIds(null), "ownedIngredientIds", List.of(999999999L)), null).getResponse().getStatus());

    }

}
