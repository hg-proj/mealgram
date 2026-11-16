package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRole;
import com.mealgram.meal.nutrition.NutritionSummary;

class MealNutritionTest {

    private ComposedMeal balancedMeal() {

        return DishFixtures.meal(true,
                DishFixtures.dish("쌀밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.dish("제육볶음", DishRole.MAIN, 250, 20, 12, 15));

    }

    @Test
    @DisplayName("요리의 영양을 더해 합계와 비율을 계산한다.")
    void sumsNutrition() {

        NutritionSummary summary = MealNutrition.summarize(balancedMeal(), 550, DietStyle.BALANCED);

        assertEquals(0, new BigDecimal("550.0").compareTo(summary.calorie()));
        assertEquals(0, new BigDecimal("80.0").compareTo(summary.carbohydrate()));
        assertEquals(0, new BigDecimal("26.0").compareTo(summary.protein()));
        assertEquals(0, new BigDecimal("13.0").compareTo(summary.fat()));
        assertEquals(550, summary.targetCalorie());

    }

    @Test
    @DisplayName("열량이 목표보다 30% 넘게 벗어나면 문제로 알려준다.")
    void reportsCalorieIssue() {

        NutritionSummary high = MealNutrition.summarize(balancedMeal(), 300, DietStyle.BALANCED);
        NutritionSummary low = MealNutrition.summarize(balancedMeal(), 900, DietStyle.BALANCED);

        assertFalse(high.passed());
        assertTrue(high.issues().stream().anyMatch(issue -> issue.contains("높습니다")));
        assertFalse(low.passed());
        assertTrue(low.issues().stream().anyMatch(issue -> issue.contains("낮습니다")));

    }

    @Test
    @DisplayName("영양 비율이 식단 성향의 기준을 벗어나면 문제로 알려준다.")
    void reportsRatioIssue() {

        ComposedMeal fatty = DishFixtures.meal(true,
                DishFixtures.dish("튀김", DishRole.MAIN, 500, 5, 40, 20));

        NutritionSummary summary = MealNutrition.summarize(fatty, 500, DietStyle.LOW_FAT);

        assertFalse(summary.passed());
        assertTrue(summary.issues().stream().anyMatch(issue -> issue.startsWith("지방")));

    }

    @Test
    @DisplayName("추정치로 채운 요리가 하나라도 있으면 검증되지 않은 영양으로 표시한다.")
    void marksEstimatedAsUnverified() {

        Dish estimated = Dish.builder().id(99L).name("추정요리").role(DishRole.SIDE).cuisine("한식")
                .mainIngredients("").methods("").flags("").servingGram(100)
                .calorie(BigDecimal.TEN).protein(BigDecimal.ONE).fat(BigDecimal.ONE).carbohydrate(BigDecimal.ONE)
                .source("분류 평균 반찬").frequency(1).build();

        NutritionSummary verified = MealNutrition.summarize(balancedMeal(), 550, DietStyle.BALANCED);
        NutritionSummary unverified = MealNutrition.summarize(
                new ComposedMeal(List.of(new ComposedDish(estimated, 1)), true), 550, DietStyle.BALANCED);

        assertTrue(verified.verified());
        assertFalse(unverified.verified());

    }

}
