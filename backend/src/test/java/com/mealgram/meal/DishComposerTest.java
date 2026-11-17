package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRepository;
import com.mealgram.dish.DishRole;

class DishComposerTest {

    private DishRepository repository;
    private DishComposer composer;

    @BeforeEach
    void setUp() {

        List<Dish> dishes = new ArrayList<>(List.of(
                DishFixtures.dish("쌀밥", DishRole.RICE, 230, 4, 0.5, 50),
                DishFixtures.dish("현미밥", DishRole.RICE, 235, 5, 1, 50),
                DishFixtures.dish("미역국", DishRole.SOUP, 40, 3, 1, 5),
                DishFixtures.dish("콩나물국", DishRole.SOUP, 35, 3, 1, 4),
                DishFixtures.dish("된장국", DishRole.SOUP, 45, 3, 1.5, 5),
                DishFixtures.dish("닭가슴살구이", DishRole.MAIN, "한식", 160, 28, 4, 2, List.of("닭고기"), List.of("구이"), List.of()),
                DishFixtures.dish("닭강정", DishRole.MAIN, "한식", 180, 15, 10, 12, List.of("닭고기"), List.of("튀김"), List.of("튀김", "고지방")),
                DishFixtures.dish("두부조림", DishRole.MAIN, "한식", 150, 18, 6, 6, List.of("두부"), List.of("조림"), List.of()),
                DishFixtures.dish("생선구이", DishRole.MAIN, "한식", 150, 22, 6, 0, List.of("생선"), List.of("구이"), List.of()),
                DishFixtures.dish("삼겹살구이", DishRole.MAIN, "한식", 180, 12, 14, 0, List.of("돼지고기"), List.of("구이"), List.of("고지방")),
                DishFixtures.dish("시금치나물", DishRole.SIDE, 30, 2, 1.5, 3),
                DishFixtures.dish("콩나물무침", DishRole.SIDE, 30, 2, 1.5, 3),
                DishFixtures.dish("애호박볶음", DishRole.SIDE, 40, 1.5, 3, 4),
                DishFixtures.dish("버섯구이", DishRole.SIDE, 40, 3, 3, 3),
                DishFixtures.dish("배추김치", DishRole.PICKLE, 10, 0.5, 0.1, 1.5),
                DishFixtures.dish("깍두기", DishRole.PICKLE, 12, 0.5, 0.1, 2),
                DishFixtures.dish("사과", DishRole.DESSERT, 50, 0.3, 0.2, 13)));
        repository = mock(DishRepository.class);
        when(repository.findAll()).thenReturn(dishes);
        composer = new DishComposer(repository);

    }

    private MealCondition condition(String goal, int target, List<String> required) {

        return new MealCondition(goal, DietStyle.BALANCED, target, required, List.of());

    }

    private long count(ComposedMeal meal, DishRole role) {

        return meal.dishes().stream().filter(composed -> composed.dish().getRole() == role).count();

    }

    @Test
    @DisplayName("밥, 국, 메인 반찬 한 개와 곁들임 반찬 두 개를 구성한다.")
    void composesBasicStructure() {

        List<ComposedMeal> meals = composer.compose(condition("유지", 560, List.of()), 6, new Random(1));

        assertFalse(meals.isEmpty());
        for (ComposedMeal meal : meals) {
            assertEquals(1, count(meal, DishRole.RICE));
            assertEquals(1, count(meal, DishRole.SOUP));
            assertEquals(1, count(meal, DishRole.MAIN));
            assertEquals(2, count(meal, DishRole.SIDE));
        }

    }

    @Test
    @DisplayName("다이어트에는 튀김과 고지방 메인 반찬을 넣지 않는다.")
    void dietExcludesFriedAndFatty() {

        List<ComposedMeal> meals = composer.compose(condition("다이어트", 500, List.of()), 20, new Random(2));

        assertTrue(meals.stream().flatMap(meal -> meal.dishes().stream())
                .noneMatch(composed -> composed.dish().hasFlag(Dish.FRIED) || composed.dish().hasFlag(Dish.FATTY)));

    }

    @Test
    @DisplayName("한 끼에 같은 주재료의 요리를 두 번 넣지 않는다.")
    void avoidsRepeatedMainIngredient() {

        List<ComposedMeal> meals = composer.compose(condition("유지", 560, List.of()), 20, new Random(3));

        for (ComposedMeal meal : meals) {
            List<String> ingredients = meal.dishes().stream()
                    .flatMap(composed -> composed.dish().mainIngredientList().stream()).toList();
            assertEquals(ingredients.size(), Set.copyOf(ingredients).size());
        }

    }

    @Test
    @DisplayName("필수 재료가 들어간 요리가 있으면 그 요리를 넣는다.")
    void containsRequiredIngredient() {

        List<ComposedMeal> meals = composer.compose(condition("유지", 560, List.of("두부")), 5, new Random(4));

        assertFalse(meals.isEmpty());
        for (ComposedMeal meal : meals) {
            assertTrue(meal.requiredMet());
            assertTrue(meal.dishes().stream().anyMatch(composed -> composed.dish().mainIngredientList().contains("두부")));
        }

    }

    @Test
    @DisplayName("필수 재료가 들어간 요리가 없으면 조건을 풀어 구성하고 충족하지 못했다고 표시한다.")
    void relaxesWhenRequiredIngredientIsMissing() {

        List<ComposedMeal> meals = composer.compose(condition("유지", 560, List.of("랍스터")), 3, new Random(5));

        assertFalse(meals.isEmpty());
        assertTrue(meals.stream().noneMatch(ComposedMeal::requiredMet));

    }

    @Test
    @DisplayName("필수 재료를 쓴 요리가 하나뿐이면 같은 사진의 식단이 겹치지 않게 나머지는 다른 요리로 구성한다.")
    void avoidsSamePhotoWhenRequiredMainIsSingle() {

        List<Dish> dishes = new ArrayList<>(List.of(
                DishFixtures.dish("쌀밥", DishRole.RICE, 230, 4, 0.5, 50),
                DishFixtures.dish("현미밥", DishRole.RICE, 235, 5, 1, 50),
                DishFixtures.dish("미역국", DishRole.SOUP, 40, 3, 1, 5),
                DishFixtures.dish("콩나물국", DishRole.SOUP, 35, 3, 1, 4),
                DishFixtures.mainWithPhoto("두부조림", "p1", List.of("두부")),
                DishFixtures.mainWithPhoto("생선구이", "p2", List.of("생선")),
                DishFixtures.mainWithPhoto("닭가슴살구이", "p3", List.of("닭고기")),
                DishFixtures.mainWithPhoto("삼겹살구이", "p4", List.of("돼지고기")),
                DishFixtures.dish("시금치나물", DishRole.SIDE, 30, 2, 1.5, 3),
                DishFixtures.dish("콩나물무침", DishRole.SIDE, 30, 2, 1.5, 3),
                DishFixtures.dish("애호박볶음", DishRole.SIDE, 40, 1.5, 3, 4),
                DishFixtures.dish("버섯구이", DishRole.SIDE, 40, 3, 3, 3),
                DishFixtures.dish("배추김치", DishRole.PICKLE, 10, 0.5, 0.1, 1.5),
                DishFixtures.dish("깍두기", DishRole.PICKLE, 12, 0.5, 0.1, 2),
                DishFixtures.dish("사과", DishRole.DESSERT, 50, 0.3, 0.2, 13)));
        when(repository.findAll()).thenReturn(dishes);
        composer = new DishComposer(repository);

        List<ComposedMeal> meals = composer.compose(condition("유지", 560, List.of("두부")), 4, new Random(7));

        List<String> photos = meals.stream().flatMap(meal -> meal.photoGroups().stream()).toList();
        assertEquals(4, meals.size());
        assertEquals(photos.size(), photos.stream().distinct().count());
        assertTrue(meals.get(0).requiredMet());

    }

    @Test
    @DisplayName("일품식이 있으면 후보 여덟 개 중 일부를 일품과 가벼운 곁들임으로 구성한다.")
    void composesOneDishMeals() {

        List<Dish> dishes = new ArrayList<>(repository.findAll());
        dishes.add(DishFixtures.oneDish("잔치국수", 470, Dish.NOODLE, "배추김치"));
        dishes.add(DishFixtures.oneDish("카레라이스", 520, Dish.RICE_BOWL, "깍두기,된장국"));
        when(repository.findAll()).thenReturn(dishes);

        List<ComposedMeal> meals = new DishComposer(repository).compose(condition("유지", 560, List.of()), 8, new Random(7));

        List<ComposedMeal> oneDishMeals = meals.stream().filter(meal -> count(meal, DishRole.ONE_DISH) == 1).toList();
        assertFalse(oneDishMeals.isEmpty());
        assertTrue(meals.indexOf(oneDishMeals.get(0)) <= 2);
        for (ComposedMeal meal : oneDishMeals) {
            assertEquals(0, count(meal, DishRole.MAIN));
            assertEquals(0, count(meal, DishRole.RICE));
            Dish one = meal.dishes().get(0).dish();
            for (String paired : one.pairingList()) {
                assertTrue(meal.dishes().stream().anyMatch(composed -> composed.dish().getName().equals(paired)),
                        one.getName() + "에 " + paired + "가 빠짐");
            }
            assertTrue(Math.abs(meal.calorie() - 560) / 560 <= 0.06, "열량 " + meal.calorie());
        }

    }

    @Test
    @DisplayName("필수 재료가 있으면 그 재료가 든 일품식만 넣는다.")
    void oneDishRespectsRequiredIngredient() {

        List<Dish> dishes = new ArrayList<>(repository.findAll());
        dishes.add(DishFixtures.oneDish("잔치국수", 480, Dish.NOODLE, "배추김치"));
        when(repository.findAll()).thenReturn(dishes);

        List<ComposedMeal> meals = new DishComposer(repository).compose(condition("유지", 560, List.of("두부")), 8, new Random(8));

        assertTrue(meals.stream().noneMatch(meal -> count(meal, DishRole.ONE_DISH) > 0));

    }

    @Test
    @DisplayName("요리가 하나도 없으면 빈 목록을 돌려준다.")
    void returnsEmptyWithoutDishes() {

        when(repository.findAll()).thenReturn(List.of());

        assertTrue(new DishComposer(repository).compose(condition("유지", 560, List.of()), 3, new Random(6)).isEmpty());

    }

}
