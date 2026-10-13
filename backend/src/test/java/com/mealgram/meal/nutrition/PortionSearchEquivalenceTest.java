package com.mealgram.meal.nutrition;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.recipe.Recipe;

class PortionSearchEquivalenceTest {

    private Recipe recipe(BigDecimal calorie, BigDecimal carbohydrate, BigDecimal protein, BigDecimal fat) {

        return Recipe.builder().category("반찬").calorie(calorie).carbohydrate(carbohydrate).protein(protein).fat(fat).build();

    }

    private BigDecimal twoDecimals(Random random, int maxWhole) {

        return BigDecimal.valueOf(random.nextInt(maxWhole * 100 + 1), 2);

    }

    @Test
    @DisplayName("빠른 계산과 느린 계산(소수 셋째 자리 값으로 강제)이 모든 경우에서 같은 분량을 고른다.")
    void fastAndSlowPathsPickSamePortions() {

        Random random = new Random(20261007);
        for (int round = 0; round < 300; round++) {
            int size = 1 + random.nextInt(5);
            List<Recipe> fast = new ArrayList<>();
            List<Recipe> slow = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                BigDecimal calorie = twoDecimals(random, 600);
                BigDecimal carbohydrate = twoDecimals(random, 90);
                BigDecimal protein = twoDecimals(random, 40);
                BigDecimal fat = twoDecimals(random, 40);
                fast.add(recipe(calorie, carbohydrate, protein, fat));
                slow.add(recipe(calorie.setScale(3), carbohydrate.setScale(3), protein.setScale(3), fat.setScale(3)));
            }
            int target = 200 + random.nextInt(900);

            assertArrayEquals(MealNutritionValidator.portions(slow, target), MealNutritionValidator.portions(fast, target),
                    "라운드 " + round + ", 요리 " + size + "개, 목표 " + target);
        }

    }

    @Test
    @DisplayName("합계가 소수 둘째 자리 반올림 경계에 걸리는 값에서도 같은 분량을 고른다.")
    void sameResultOnRoundingBoundaries() {

        List<Recipe> fast = List.of(
                recipe(new BigDecimal("123.45"), new BigDecimal("12.35"), new BigDecimal("5.55"), new BigDecimal("3.25")),
                recipe(new BigDecimal("87.65"), new BigDecimal("9.15"), new BigDecimal("2.05"), new BigDecimal("1.95")));
        List<Recipe> slow = fast.stream().map(r -> recipe(r.getCalorie().setScale(3), r.getCarbohydrate().setScale(3),
                r.getProtein().setScale(3), r.getFat().setScale(3))).toList();

        for (int target = 100; target <= 700; target += 7) {
            assertArrayEquals(MealNutritionValidator.portions(slow, target), MealNutritionValidator.portions(fast, target));
        }

    }

    @Test
    @DisplayName("값이 없는 요리가 있으면 기본 분량 1.0을 돌려준다.")
    void unknownNutritionKeepsDefaultPortions() {

        List<Recipe> recipes = List.of(recipe(null, null, null, null),
                recipe(new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("3.00")));

        assertArrayEquals(new double[] {1.0, 1.0}, MealNutritionValidator.portions(recipes, 700));
        assertEquals(0, MealNutritionValidator.portions(List.of(), 700).length);

    }

}
