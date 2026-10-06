package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.recipe.dto.RecipeCandidate;

class MealComposerTest {

    private RecipeCandidate candidate(long id, String category) {

        return candidate(id, "요리" + id, category);

    }

    private RecipeCandidate unknownNutrition(long id, String category) {

        return new RecipeCandidate(id, "요리" + id, category, "", null, null, null, null, null, List.of(), 0.9, 0, true);

    }

    private RecipeCandidate candidate(long id, String name, String category) {

        return new RecipeCandidate(id, name, category, "", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, List.of(), 0.5, 0, true);

    }

    private List<RecipeCandidate> pool() {

        List<RecipeCandidate> pool = new ArrayList<>();
        pool.add(candidate(1, "밥"));
        pool.add(candidate(2, "밥"));
        pool.add(candidate(3, "국&찌개"));
        pool.add(candidate(4, "국&찌개"));
        for (long id = 5; id <= 10; id++) {
            pool.add(candidate(id, "반찬"));
        }
        pool.add(candidate(11, "일품"));

        return pool;

    }

    private long count(List<Long> ids, List<RecipeCandidate> pool, String category) {

        return pool.stream().filter(c -> ids.contains(c.id()) && c.category().equals(category)).count();

    }

    @Test
    @DisplayName("빠진 칸을 후보에서 채운다.")
    void fillsMissingSlotsFromCandidates() {

        List<Long> result = MealComposer.compose(List.of(5L), pool(), new HashSet<>());

        assertEquals(1, count(result, pool(), "밥"));
        assertEquals(1, count(result, pool(), "국&찌개"));
        assertEquals(3, count(result, pool(), "반찬"));

    }

    @Test
    @DisplayName("일품이 있으면 밥을 채우지 않는다.")
    void doesNotFillRiceWhenMainDishExists() {

        List<Long> result = MealComposer.compose(List.of(11L), pool(), new HashSet<>());

        assertEquals(0, count(result, pool(), "밥"));
        assertEquals(1, count(result, pool(), "일품"));

    }

    @Test
    @DisplayName("이미 충분하면 그대로 둔다.")
    void keepsPickedWhenComplete() {

        List<Long> picked = List.of(1L, 3L, 5L, 6L, 7L);

        assertEquals(picked, MealComposer.compose(picked, pool(), new HashSet<>()));

    }

    @Test
    @DisplayName("다른 식단안에서 쓴 요리를 먼저 피한다.")
    void avoidsRecipesUsedInOtherMeals() {

        Set<Long> used = new HashSet<>(List.of(1L, 3L, 5L, 6L, 7L));

        List<Long> result = MealComposer.compose(List.of(), pool(), used);

        assertTrue(result.contains(2L));
        assertTrue(result.contains(4L));
        assertTrue(result.stream().noneMatch(id -> id == 5L || id == 6L || id == 7L));

    }

    @Test
    @DisplayName("넘치는 요리는 구성에 맞게 줄인다.")
    void trimsExcessRecipes() {

        List<Long> picked = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 11L);

        List<Long> result = MealComposer.compose(picked, pool(), new HashSet<>());

        assertEquals(1, count(result, pool(), "밥") + count(result, pool(), "일품"));
        assertEquals(1, count(result, pool(), "국&찌개"));
        assertEquals(3, count(result, pool(), "반찬"));
        assertEquals(5, result.size());

    }

    @Test
    @DisplayName("같은 이름의 요리는 한 식단안에 넣지 않는다.")
    void skipsRecipesWithSameName() {

        List<RecipeCandidate> pool = new ArrayList<>(pool());
        pool.add(candidate(20, "요리5", "반찬"));

        List<Long> result = MealComposer.compose(List.of(5L, 20L), pool, new HashSet<>());

        assertTrue(result.contains(5L));
        assertTrue(!result.contains(20L));

    }

    @Test
    @DisplayName("빈 칸은 영양 정보가 있는 요리부터 채운다.")
    void fillsWithKnownNutritionFirst() {

        List<RecipeCandidate> pool = new ArrayList<>();
        pool.add(unknownNutrition(30, "반찬"));
        pool.add(unknownNutrition(31, "반찬"));
        pool.add(candidate(32, "반찬"));
        pool.add(candidate(33, "반찬"));
        pool.add(candidate(34, "반찬"));
        pool.add(candidate(1, "밥"));
        pool.add(candidate(3, "국&찌개"));

        List<Long> result = MealComposer.compose(List.of(), pool, new HashSet<>());

        assertTrue(result.containsAll(List.of(32L, 33L, 34L)));
        assertTrue(!result.contains(30L) && !result.contains(31L));

    }

}
