package com.mealgram.meal.nutrition;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.recipe.Recipe;

class MealNutritionValidatorTest {

    private Recipe recipe(String category, String calorie, String carbohydrate, String protein, String fat) {

        return Recipe.builder()
                .category(category)
                .calorie(new BigDecimal(calorie))
                .carbohydrate(new BigDecimal(carbohydrate))
                .protein(new BigDecimal(protein))
                .fat(new BigDecimal(fat))
                .build();

    }

    private double[] ones(int size) {

        double[] portions = new double[size];
        Arrays.fill(portions, 1.0);

        return portions;

    }

    @Test
    @DisplayName("기준을 모두 만족하면 통과한다.")
    void passesWhenAllRulesAreMet() {

        List<Recipe> recipes = List.of(
                recipe("밥", "300", "62", "6", "1"),
                recipe("국&찌개", "100", "10", "6", "3"),
                recipe("반찬", "250", "25", "16", "12"));

        NutritionSummary result = MealNutritionValidator.validate(recipes, ones(recipes.size()), 650);

        assertTrue(result.passed());
        assertTrue(result.issues().isEmpty());
        assertEquals(0, new BigDecimal("650").compareTo(result.calorie()));

    }

    @Test
    @DisplayName("열량이 목표보다 높으면 사유를 알린다.")
    void reportsIssueWhenCalorieIsTooHigh() {

        List<Recipe> recipes = List.of(
                recipe("밥", "800", "62", "6", "1"),
                recipe("국&찌개", "100", "10", "6", "3"),
                recipe("반찬", "250", "25", "20", "8"));

        NutritionSummary result = MealNutritionValidator.validate(recipes, ones(recipes.size()), 650);

        assertTrue(result.issues().contains("열량이 한 끼 목표(650kcal)보다 높습니다."));

    }

    @Test
    @DisplayName("끼니 구성이 부족하면 사유를 알린다.")
    void reportsIssuesWhenCompositionIsMissing() {

        List<Recipe> recipes = List.of(recipe("반찬", "250", "40", "5", "5"));

        NutritionSummary result = MealNutritionValidator.validate(recipes, ones(recipes.size()), 650);

        assertTrue(result.issues().contains("밥이나 일품 요리가 없습니다."));
        assertTrue(result.issues().contains("국이나 찌개가 없습니다."));
        assertTrue(result.issues().contains("단백질이 한 끼 권장량(20g)보다 적습니다."));

    }

    @Test
    @DisplayName("지방 비율이 높으면 사유를 알린다.")
    void reportsIssueWhenFatRatioIsTooHigh() {

        List<Recipe> recipes = List.of(
                recipe("밥", "300", "20", "5", "20"),
                recipe("국&찌개", "100", "10", "5", "10"),
                recipe("반찬", "250", "10", "20", "10"));

        NutritionSummary result = MealNutritionValidator.validate(recipes, ones(recipes.size()), 650);

        assertTrue(result.issues().contains("지방 비율이 기준(15~30%)보다 높습니다."));

    }

    @Test
    @DisplayName("열량이 목표 근처가 되도록 분량을 조절한다.")
    void adjustsPortionsNearTargetCalorie() {

        List<Recipe> recipes = List.of(
                recipe("밥", "600", "62", "6", "1"),
                recipe("국&찌개", "200", "10", "6", "3"));

        NutritionSummary result = MealNutritionValidator.validate(recipes,
                MealNutritionValidator.portions(recipes, 650), 650);

        assertTrue(result.calorie().doubleValue() >= 585 && result.calorie().doubleValue() <= 715);

    }

    @Test
    @DisplayName("분량은 하한과 상한을 넘지 않는다.")
    void keepsPortionsWithinLimits() {

        List<Recipe> large = List.of(recipe("밥", "2000", "62", "6", "1"));
        List<Recipe> small = List.of(recipe("밥", "100", "62", "6", "1"));

        assertEquals(0.5, MealNutritionValidator.portions(large, 650)[0]);
        assertEquals(1.2, MealNutritionValidator.portions(small, 650)[0]);

    }

    @Test
    @DisplayName("요리별 분량으로 탄단지 비율을 맞춘다.")
    void balancesRatioWithPerRecipePortions() {

        List<Recipe> recipes = List.of(
                recipe("밥", "349", "80", "5", "1"),
                recipe("반찬", "320", "5", "30", "20"),
                recipe("국&찌개", "67", "5", "5", "3"));

        NutritionSummary result = MealNutritionValidator.validate(recipes,
                MealNutritionValidator.portions(recipes, 650), 650);

        assertTrue(result.passed());

    }

    @Test
    @DisplayName("분량을 반영해 합계를 계산한다.")
    void appliesPortionsToTotals() {

        List<Recipe> recipes = List.of(recipe("밥", "600", "62", "6", "1"));

        NutritionSummary result = MealNutritionValidator.validate(recipes, new double[] {0.5}, 300);

        assertEquals(0, new BigDecimal("300").compareTo(result.calorie()));

    }

    @Test
    @DisplayName("영양 정보가 없는 요리가 있으면 검증하지 않는다.")
    void skipsValidationWhenNutritionIsUnknown() {

        List<Recipe> recipes = List.of(
                recipe("밥", "300", "62", "6", "1"),
                Recipe.builder().category("국&찌개").build());

        NutritionSummary result = MealNutritionValidator.validate(recipes, ones(recipes.size()), 650);

        assertFalse(result.verified());
        assertFalse(result.passed());
        assertNull(result.calorie());
        assertEquals(List.of("영양 정보가 없는 요리가 있어 영양 검증을 하지 못했습니다."), result.issues());

    }

    @Test
    @DisplayName("영양 정보가 없는 요리가 있으면 분량을 조절하지 않는다.")
    void keepsPortionsWhenNutritionIsUnknown() {

        List<Recipe> recipes = List.of(
                recipe("밥", "300", "62", "6", "1"),
                Recipe.builder().category("반찬").build());

        assertArrayEquals(new double[] {1.0, 1.0}, MealNutritionValidator.portions(recipes, 650));

    }

    @Test
    @DisplayName("탄수화물, 단백질, 지방의 에너지 비율을 계산한다.")
    void calculatesEnergyRatios() {

        List<Recipe> recipes = List.of(recipe("밥", "500", "60", "20", "20"));

        NutritionSummary result = MealNutritionValidator.validate(recipes, ones(recipes.size()), 650);

        assertEquals(0, new BigDecimal("48.0").compareTo(result.carbohydrateRatio()));
        assertEquals(0, new BigDecimal("16.0").compareTo(result.proteinRatio()));
        assertEquals(0, new BigDecimal("36.0").compareTo(result.fatRatio()));

    }

}
