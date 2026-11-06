package com.mealgram.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;

@EnabledIf("com.mealgram.integration.IntegrationTestSupport#dockerAvailable")
class MealSampleIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("로그인 없이 랜덤 식단 세 개를 조회한다.")
    void returnsSamplesWithoutLogin() throws Exception {

        MvcResult result = getJson("/meals/samples", null);
        JsonNode samples = body(result);

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(3, samples.size());
        assertEquals("다이어트", samples.get(0).get("goal").asString());
        assertEquals("유지", samples.get(1).get("goal").asString());
        assertEquals("벌크업", samples.get(2).get("goal").asString());
        for (JsonNode sample : samples) {
            assertTrue(sample.get("recipes").size() >= 4);
        }

    }

    @Test
    @DisplayName("랜덤 식단의 메인 요리에는 사진 경로가 있다.")
    void mainDishHasImage() throws Exception {

        JsonNode samples = body(getJson("/meals/samples", null));

        for (JsonNode sample : samples) {
            boolean hasImage = false;
            for (JsonNode recipe : sample.get("recipes")) {
                hasImage = hasImage || recipe.get("imageUrl").asString("").startsWith("/images/recipes/");
            }
            assertTrue(hasImage);
        }

    }

}
