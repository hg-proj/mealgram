package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.dish.DishRole;

class MealPromptTest {

    @Test
    @DisplayName("후보 식단마다 번호와 요리 이름, 분량, 영양 비율을 적는다.")
    void listsCandidatesWithIndex() {

        ComposedMeal meal = DishFixtures.meal(true,
                DishFixtures.dish("쌀밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.dish("제육볶음", DishRole.MAIN, 250, 20, 12, 15));

        String prompt = MealPrompt.user(List.of(meal, meal), "유지", DietStyle.HIGH_PROTEIN, null, 550);

        assertTrue(prompt.contains("번호=0"));
        assertTrue(prompt.contains("번호=1"));
        assertTrue(prompt.contains("쌀밥 1g") || prompt.contains("쌀밥"));
        assertTrue(prompt.contains("제육볶음"));
        assertTrue(prompt.contains("고단백"));
        assertTrue(prompt.contains("선택 안 함"));
        assertTrue(prompt.contains("550kcal"));

    }

    @Test
    @DisplayName("시스템 프롬프트에는 식단 개수가 들어가고 레시피 말은 없다.")
    void systemPrompt() {

        String system = MealPrompt.system();

        assertTrue(system.contains(MealPrompt.MEAL_COUNT + "개"));
        assertFalse(system.contains("레시피"));

    }

}
