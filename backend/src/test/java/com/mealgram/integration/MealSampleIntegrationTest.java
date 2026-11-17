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

        MvcResult result = getJson("/api/meals/samples", null);
        JsonNode samples = body(result);

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(3, samples.size());
        assertEquals("다이어트", samples.get(0).get("goal").asString());
        assertEquals("유지", samples.get(1).get("goal").asString());
        assertEquals("벌크업", samples.get(2).get("goal").asString());
        for (JsonNode sample : samples) {
            assertTrue(sample.get("dishes").size() >= 1);
        }

    }

    @Test
    @DisplayName("랜덤 식단의 요리에는 분량과 열량이 있고 사진 경로는 요리 사진 폴더를 가리킨다.")
    void dishesHaveAmountAndCalorie() throws Exception {

        JsonNode samples = body(getJson("/api/meals/samples", null));

        for (JsonNode sample : samples) {
            for (JsonNode dish : sample.get("dishes")) {
                assertTrue(dish.get("grams").asInt() > 0);
                assertTrue(dish.get("calorie").asInt() > 0);
                assertTrue(dish.get("imageUrl").isNull() || dish.get("imageUrl").asString().startsWith("/images/dishes/"));
            }
        }

    }

}
