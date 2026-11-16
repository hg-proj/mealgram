package com.mealgram.meal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.mealgram.meal.nutrition.NutritionSummary;

// 식단 영양 합계와 기준 확인

public final class MealNutrition {

    private static final double CALORIE_MIN_RATE = 0.7;
    private static final double CALORIE_MAX_RATE = 1.3;
    private static final double CARBOHYDRATE_KCAL_PER_GRAM = 4;
    private static final double PROTEIN_KCAL_PER_GRAM = 4;
    private static final double FAT_KCAL_PER_GRAM = 9;
    private static final String ESTIMATED_SOURCE = "분류 평균";

    private MealNutrition() {

    }

    public static NutritionSummary summarize(ComposedMeal meal, int targetCalorie, DietStyle style) {

        double calorie = meal.calorie();
        double carbohydrate = meal.carbohydrate();
        double protein = meal.protein();
        double fat = meal.fat();
        double energy = carbohydrate * CARBOHYDRATE_KCAL_PER_GRAM + protein * PROTEIN_KCAL_PER_GRAM + fat * FAT_KCAL_PER_GRAM;

        List<String> issues = new ArrayList<>();
        checkCalorie(calorie, targetCalorie, issues);
        if (energy > 0) {
            checkRange("탄수화물", carbohydrate * CARBOHYDRATE_KCAL_PER_GRAM / energy * 100, style.getCarbohydrateMin(),
                    style.getCarbohydrateMax(), issues);
            checkRange("단백질", protein * PROTEIN_KCAL_PER_GRAM / energy * 100, style.getProteinMin(), style.getProteinMax(),
                    issues);
            checkRange("지방", fat * FAT_KCAL_PER_GRAM / energy * 100, style.getFatMin(), style.getFatMax(), issues);
        }
        boolean verified = meal.dishes().stream().noneMatch(composed -> composed.dish().getSource().startsWith(ESTIMATED_SOURCE));

        return new NutritionSummary(scaled(calorie), scaled(carbohydrate), scaled(protein), scaled(fat),
                ratio(carbohydrate * CARBOHYDRATE_KCAL_PER_GRAM, energy), ratio(protein * PROTEIN_KCAL_PER_GRAM, energy),
                ratio(fat * FAT_KCAL_PER_GRAM, energy), targetCalorie, verified, issues.isEmpty(), issues);

    }

    private static BigDecimal scaled(double value) {

        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);

    }

    private static BigDecimal ratio(double part, double energy) {

        if (energy == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(part / energy * 100).setScale(1, RoundingMode.HALF_UP);

    }

    private static void checkCalorie(double calorie, int targetCalorie, List<String> issues) {

        if (calorie > targetCalorie * CALORIE_MAX_RATE) {
            issues.add("열량이 한 끼 목표(" + targetCalorie + "kcal)보다 높습니다.");
        } else if (calorie < targetCalorie * CALORIE_MIN_RATE) {
            issues.add("열량이 한 끼 목표(" + targetCalorie + "kcal)보다 낮습니다.");
        }

    }

    private static void checkRange(String name, double percent, double min, double max, List<String> issues) {

        String standard = name + " 비율이 기준(" + (int) min + "~" + (int) max + "%)보다 ";
        if (percent > max) {
            issues.add(standard + "높습니다.");
        } else if (percent < min) {
            issues.add(standard + "낮습니다.");
        }

    }

}
