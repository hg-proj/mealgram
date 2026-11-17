package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.mealgram.batch.DishSeed;
import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRepository;
import com.mealgram.dish.DishRole;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// 실제 시드 요리로 여러 번 구성해 규칙 위반 확인

class DishComposerSeedTest {

    private static final int RUNS = 40;
    private static final int[] TARGETS = {500, 667, 767};
    private static final String[] GOALS = {"다이어트", "유지", "벌크업"};

    private static DishComposer composer;

    @BeforeAll
    static void load() throws Exception {

        List<DishSeed> seeds;
        try (InputStream in = new ClassPathResource("seed/dishes.json").getInputStream()) {
            seeds = JsonMapper.builder().build().readValue(in, new TypeReference<List<DishSeed>>() {});
        }
        long id = 1;
        List<Dish> dishes = new java.util.ArrayList<>();
        for (DishSeed seed : seeds) {
            dishes.add(Dish.builder()
                    .id(id++)
                    .name(seed.name())
                    .role(DishRole.valueOf(seed.role()))
                    .cuisine(seed.cuisine())
                    .mainIngredients(String.join(Dish.LIST_SEPARATOR, seed.mainIngredients()))
                    .methods(String.join(Dish.LIST_SEPARATOR, seed.methods()))
                    .flags(String.join(Dish.LIST_SEPARATOR, seed.flags()))
                    .dessertType(seed.dessertType())
                    .imageGroup(seed.imageGroup())
                    .heavySoup(seed.heavySoup())
                    .servingGram(seed.servingGram())
                    .calorie(seed.calorie())
                    .protein(seed.protein())
                    .fat(seed.fat())
                    .carbohydrate(seed.carbohydrate())
                    .source(seed.source())
                    .frequency(seed.frequency())
                    .build());
        }
        DishRepository repository = mock(DishRepository.class);
        when(repository.findAll()).thenReturn(dishes);
        composer = new DishComposer(repository);

    }

    private long count(ComposedMeal meal, DishRole role) {

        return meal.dishes().stream().filter(c -> c.dish().getRole() == role).count();

    }

    private void check(String goal, int target, DietStyle style, double minSuccessRate) {

        int made = 0;
        Random random = new Random(goal.hashCode() + style.ordinal());
        for (int i = 0; i < RUNS; i++) {
            List<ComposedMeal> meals = composer.compose(new MealCondition(goal, style, target, List.of(), List.of()), 1, random);
            if (meals.isEmpty()) {
                continue;
            }
            made++;
            ComposedMeal meal = meals.get(0);
            String description = goal + " " + style + " " + meal.dishes().stream().map(c -> c.dish().getName()).toList();
            assertTrue(Math.abs(meal.calorie() - target) / target <= 0.06, "열량 " + description);
            assertEquals(1, count(meal, DishRole.RICE), description);
            assertEquals(1, count(meal, DishRole.SOUP), description);
            assertEquals(1, count(meal, DishRole.MAIN), description);
            assertEquals(2, count(meal, DishRole.SIDE), description);
            assertTrue(meal.dishes().stream().filter(c -> c.dish().hasFlag(Dish.FRIED)).count() <= 1, "튀김 " + description);
            List<String> ingredients = meal.dishes().stream().filter(c -> c.dish().getRole() != DishRole.RICE && c.dish().getRole() != DishRole.PICKLE)
                    .flatMap(c -> c.dish().mainIngredientList().stream()).toList();
            assertEquals(ingredients.size(), Set.copyOf(ingredients).size(), "주재료 중복 " + description);
            if ("다이어트".equals(goal)) {
                assertTrue(meal.dishes().stream().noneMatch(c -> c.dish().hasFlag(Dish.FRIED)), "다이어트 튀김 " + description);
            }
        }
        assertTrue(made >= RUNS * minSuccessRate, goal + " " + style + " 성공 " + made + "/" + RUNS);

    }

    @Test
    @DisplayName("균형 식단은 목표별로 여러 번 구성해도 규칙을 지킨다.")
    void balancedFollowsRules() {

        for (int i = 0; i < GOALS.length; i++) {
            check(GOALS[i], TARGETS[i], DietStyle.BALANCED, 0.95);
        }

    }

    @Test
    @DisplayName("고단백, 저지방 식단도 목표별로 여러 번 구성해도 규칙을 지킨다.")
    void proteinAndLowFatFollowRules() {

        for (int i = 0; i < GOALS.length; i++) {
            check(GOALS[i], TARGETS[i], DietStyle.HIGH_PROTEIN, 0.9);
            check(GOALS[i], TARGETS[i], DietStyle.LOW_FAT, 0.9);
        }

    }

    @Test
    @DisplayName("체격이 큰 사람의 벌크업 목표 열량에서도 모든 식단 성향으로 구성한다.")
    void bulkUpWithLargeTarget() {

        for (DietStyle style : DietStyle.values()) {
            check("벌크업", 1080, style, 0.8);
        }

    }

    @Test
    @DisplayName("저탄수 식단은 대부분 구성하고 규칙을 지킨다.")
    void lowCarbFollowsRules() {

        for (int i = 0; i < GOALS.length; i++) {
            check(GOALS[i], TARGETS[i], DietStyle.LOW_CARB, 0.8);
        }

    }

}
