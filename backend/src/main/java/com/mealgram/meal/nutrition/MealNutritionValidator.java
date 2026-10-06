package com.mealgram.meal.nutrition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.mealgram.recipe.Recipe;

// 식단안 영양 검증

public final class MealNutritionValidator {

    private static final double PORTION_MIN = 0.5;
    private static final double PORTION_MAX = 1.2;
    private static final double PORTION_STEP = 0.1;
    private static final int MAX_SEARCH_SIZE = 6;
    private static final double FREE_CALORIE_GAP = 0.1;
    private static final double CALORIE_MIN_RATE = 0.7;
    private static final double CALORIE_MAX_RATE = 1.3;
    private static final double CARBOHYDRATE_MIN = 50;
    private static final double CARBOHYDRATE_MAX = 65;
    private static final double PROTEIN_MIN = 10;
    private static final double PROTEIN_MAX = 20;
    private static final double FAT_MIN = 15;
    private static final double FAT_MAX = 30;
    private static final double MEAL_PROTEIN_MIN_GRAM = 20;
    private static final double CARBOHYDRATE_KCAL_PER_GRAM = 4;
    private static final double PROTEIN_KCAL_PER_GRAM = 4;
    private static final double FAT_KCAL_PER_GRAM = 9;
    private static final List<String> STAPLE_CATEGORIES = List.of("밥", "일품");
    private static final String SOUP_CATEGORY = "국&찌개";
    private static final String NO_NUTRITION_ISSUE = "영양 정보가 없는 요리가 있어 영양 검증을 하지 못했습니다.";

    private MealNutritionValidator() {

    }

    public static double[] portions(List<Recipe> recipes, int targetCalorie) {

        int size = recipes.size();
        double[] best = new double[size];
        Arrays.fill(best, 1.0);
        if (size == 0 || size > MAX_SEARCH_SIZE || hasUnknownNutrition(recipes)) {
            return best;
        }

        int levels = (int) Math.round((PORTION_MAX - PORTION_MIN) / PORTION_STEP) + 1;
        int[] index = new int[size];
        double[] current = new double[size];
        double bestCost = Double.MAX_VALUE;

        for (int count = (int) Math.pow(levels, size), i = 0; i < count; i++) {
            int rest = i;
            for (int j = 0; j < size; j++) {
                index[j] = rest % levels;
                rest /= levels;
                current[j] = Math.round((PORTION_MIN + index[j] * PORTION_STEP) * 10) / 10.0;
            }
            double cost = cost(recipes, current, targetCalorie);
            if (cost < bestCost) {
                bestCost = cost;
                best = current.clone();
            }
        }

        return best;

    }

    public static NutritionSummary validate(List<Recipe> recipes, double[] portions, int targetCalorie) {

        if (hasUnknownNutrition(recipes)) {
            return new NutritionSummary(null, null, null, null, null, null, null, targetCalorie, false, false,
                    List.of(NO_NUTRITION_ISSUE));
        }

        BigDecimal calorie = sum(recipes, Recipe::getCalorie, portions);
        BigDecimal carbohydrate = sum(recipes, Recipe::getCarbohydrate, portions);
        BigDecimal protein = sum(recipes, Recipe::getProtein, portions);
        BigDecimal fat = sum(recipes, Recipe::getFat, portions);

        List<String> issues = new ArrayList<>();
        checkCalorie(calorie.doubleValue(), targetCalorie, issues);
        checkRatio(carbohydrate.doubleValue(), protein.doubleValue(), fat.doubleValue(), issues);
        checkComposition(recipes, protein.doubleValue(), issues);

        return new NutritionSummary(calorie, carbohydrate, protein, fat,
                ratio(carbohydrate, CARBOHYDRATE_KCAL_PER_GRAM, carbohydrate, protein, fat),
                ratio(protein, PROTEIN_KCAL_PER_GRAM, carbohydrate, protein, fat),
                ratio(fat, FAT_KCAL_PER_GRAM, carbohydrate, protein, fat),
                targetCalorie, true, issues.isEmpty(), issues);

    }

    private static BigDecimal ratio(BigDecimal part, double kcalPerGram, BigDecimal carbohydrate, BigDecimal protein,
                                    BigDecimal fat) {

        double energy = carbohydrate.doubleValue() * CARBOHYDRATE_KCAL_PER_GRAM + protein.doubleValue() * PROTEIN_KCAL_PER_GRAM
                + fat.doubleValue() * FAT_KCAL_PER_GRAM;
        if (energy == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(part.doubleValue() * kcalPerGram / energy * 100).setScale(1, RoundingMode.HALF_UP);

    }

    private static boolean hasUnknownNutrition(List<Recipe> recipes) {

        return recipes.stream().anyMatch(recipe -> recipe.getCalorie() == null || recipe.getCarbohydrate() == null
                || recipe.getProtein() == null || recipe.getFat() == null);

    }

    private static void checkCalorie(double calorie, int targetCalorie, List<String> issues) {

        if (calorie > targetCalorie * CALORIE_MAX_RATE) {
            issues.add("열량이 한 끼 목표(" + targetCalorie + "kcal)보다 높습니다.");
        } else if (calorie < targetCalorie * CALORIE_MIN_RATE) {
            issues.add("열량이 한 끼 목표(" + targetCalorie + "kcal)보다 낮습니다.");
        }

    }

    private static void checkRatio(double carbohydrate, double protein, double fat, List<String> issues) {

        double energy = carbohydrate * CARBOHYDRATE_KCAL_PER_GRAM + protein * PROTEIN_KCAL_PER_GRAM
                + fat * FAT_KCAL_PER_GRAM;
        if (energy == 0) {
            return;
        }

        checkRange("탄수화물", carbohydrate * CARBOHYDRATE_KCAL_PER_GRAM / energy * 100,
                CARBOHYDRATE_MIN, CARBOHYDRATE_MAX, issues);
        checkRange("단백질", protein * PROTEIN_KCAL_PER_GRAM / energy * 100, PROTEIN_MIN, PROTEIN_MAX, issues);
        checkRange("지방", fat * FAT_KCAL_PER_GRAM / energy * 100, FAT_MIN, FAT_MAX, issues);

    }

    private static void checkRange(String name, double percent, double min, double max, List<String> issues) {

        String standard = name + " 비율이 기준(" + (int) min + "~" + (int) max + "%)보다 ";
        if (percent > max) {
            issues.add(standard + "높습니다.");
        } else if (percent < min) {
            issues.add(standard + "낮습니다.");
        }

    }

    private static void checkComposition(List<Recipe> recipes, double protein, List<String> issues) {

        if (recipes.stream().noneMatch(recipe -> STAPLE_CATEGORIES.contains(recipe.getCategory()))) {
            issues.add("밥이나 일품 요리가 없습니다.");
        }
        if (recipes.stream().noneMatch(recipe -> SOUP_CATEGORY.equals(recipe.getCategory()))) {
            issues.add("국이나 찌개가 없습니다.");
        }
        if (protein < MEAL_PROTEIN_MIN_GRAM) {
            issues.add("단백질이 한 끼 권장량(" + (int) MEAL_PROTEIN_MIN_GRAM + "g)보다 적습니다.");
        }

    }

    private static double cost(List<Recipe> recipes, double[] portions, int targetCalorie) {

        double calorie = sum(recipes, Recipe::getCalorie, portions).doubleValue();
        double carbohydrate = sum(recipes, Recipe::getCarbohydrate, portions).doubleValue();
        double protein = sum(recipes, Recipe::getProtein, portions).doubleValue();
        double fat = sum(recipes, Recipe::getFat, portions).doubleValue();
        double energy = carbohydrate * CARBOHYDRATE_KCAL_PER_GRAM + protein * PROTEIN_KCAL_PER_GRAM
                + fat * FAT_KCAL_PER_GRAM;

        double cost = Math.max(0, Math.abs(calorie - targetCalorie) / targetCalorie - FREE_CALORIE_GAP) * 100;
        cost += Math.max(0, MEAL_PROTEIN_MIN_GRAM - protein);
        if (energy > 0) {
            cost += outside(carbohydrate * CARBOHYDRATE_KCAL_PER_GRAM / energy * 100, CARBOHYDRATE_MIN, CARBOHYDRATE_MAX);
            cost += outside(protein * PROTEIN_KCAL_PER_GRAM / energy * 100, PROTEIN_MIN, PROTEIN_MAX);
            cost += outside(fat * FAT_KCAL_PER_GRAM / energy * 100, FAT_MIN, FAT_MAX);
        }
        for (double portion : portions) {
            cost += Math.abs(portion - 1.0) * 0.01;
        }

        return cost;

    }

    private static double outside(double percent, double min, double max) {

        return Math.max(0, Math.max(min - percent, percent - max));

    }

    private static BigDecimal sum(List<Recipe> recipes, Function<Recipe, BigDecimal> getter, double[] portions) {

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < recipes.size(); i++) {
            total = total.add(getter.apply(recipes.get(i)).multiply(BigDecimal.valueOf(portions[i])));
        }

        return total.setScale(1, RoundingMode.HALF_UP);

    }

}
