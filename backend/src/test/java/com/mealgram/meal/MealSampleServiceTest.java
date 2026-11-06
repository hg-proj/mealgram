package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.meal.dto.MealRecipe;
import com.mealgram.meal.dto.MealSample;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.recipe.RecipeVectorRepository;
import com.mealgram.recipe.dto.RecipeCandidate;

class MealSampleServiceTest {

    private RecipeVectorRepository vectorRepository;
    private RecipeRepository recipeRepository;
    private MealSampleService service;

    private RecipeCandidate candidate(long id, String category) {

        return new RecipeCandidate(id, "요리" + id, category, "", BigDecimal.valueOf(150), BigDecimal.valueOf(20),
                BigDecimal.valueOf(15), BigDecimal.valueOf(5), BigDecimal.ZERO, List.of(), 0.0, 0, false);

    }

    private Recipe recipe(long id, String category) {

        return Recipe.builder().id(id).name("요리" + id).category(category).calorie(BigDecimal.valueOf(150))
                .carbohydrate(BigDecimal.valueOf(20)).protein(BigDecimal.valueOf(15)).fat(BigDecimal.valueOf(5))
                .imageUrl("/images/recipes/" + id + ".webp").build();

    }

    private List<RecipeCandidate> candidates(long from, long to, String category) {

        List<RecipeCandidate> list = new ArrayList<>();
        for (long id = from; id <= to; id++) {
            list.add(candidate(id, category));
        }

        return list;

    }

    @BeforeEach
    void setUp() {

        vectorRepository = mock(RecipeVectorRepository.class);
        recipeRepository = mock(RecipeRepository.class);
        service = new MealSampleService(vectorRepository, recipeRepository);

        when(vectorRepository.findRandomWithImage(any(), anyInt())).thenReturn(candidates(1, 3, "반찬"));
        when(vectorRepository.findByCategory(isNull(), eq("밥"), anyInt())).thenReturn(candidates(10, 14, "밥"));
        when(vectorRepository.findByCategory(isNull(), eq("국&찌개"), anyInt())).thenReturn(candidates(20, 24, "국&찌개"));
        when(vectorRepository.findProteinDishes(isNull(), eq("반찬"), any(), anyInt())).thenReturn(candidates(30, 37, "반찬"));
        when(vectorRepository.findByCategory(isNull(), eq("반찬"), anyInt())).thenReturn(candidates(40, 47, "반찬"));

        List<Recipe> recipes = new ArrayList<>();
        candidates(1, 3, "반찬").forEach(c -> recipes.add(recipe(c.id(), c.category())));
        candidates(10, 14, "밥").forEach(c -> recipes.add(recipe(c.id(), c.category())));
        candidates(20, 24, "국&찌개").forEach(c -> recipes.add(recipe(c.id(), c.category())));
        candidates(30, 37, "반찬").forEach(c -> recipes.add(recipe(c.id(), c.category())));
        candidates(40, 47, "반찬").forEach(c -> recipes.add(recipe(c.id(), c.category())));
        when(recipeRepository.findAllById(any())).thenReturn(recipes);

    }

    @Test
    @DisplayName("다이어트, 유지, 벌크업 순서로 식단 세 개를 돌려준다.")
    void returnsThreeMealsInGoalOrder() {

        List<MealSample> samples = service.samples();

        assertEquals(List.of("다이어트", "유지", "벌크업"), samples.stream().map(MealSample::goal).toList());
        assertTrue(samples.stream().allMatch(sample -> sample.title() != null && sample.nutrition() != null));

    }

    @Test
    @DisplayName("식단마다 밥 하나, 국 하나, 반찬 셋으로 구성한다.")
    void composesRiceSoupAndSides() {

        for (MealSample sample : service.samples()) {
            assertEquals(1, count(sample, "밥"));
            assertEquals(1, count(sample, "국&찌개"));
            assertEquals(3, count(sample, "반찬"));
        }

    }

    @Test
    @DisplayName("고른 메인 요리를 식단마다 하나씩 쓰고 식단끼리 요리가 겹치지 않는다.")
    void usesEachMainOnceWithoutOverlap() {

        List<MealSample> samples = service.samples();
        Set<Long> seen = new HashSet<>();

        for (int i = 0; i < samples.size(); i++) {
            List<Long> ids = samples.get(i).recipes().stream().map(MealRecipe::id).toList();
            assertTrue(ids.contains((long) (i + 1)));
            assertTrue(ids.stream().allMatch(seen::add));
        }

    }

    @Test
    @DisplayName("사진이 있는 단백질 요리가 모자라면 404 오류로 알린다.")
    void failsWhenNotEnoughMainDishes() {

        when(vectorRepository.findRandomWithImage(any(), anyInt())).thenReturn(candidates(1, 2, "반찬"));

        BusinessException e = assertThrows(BusinessException.class, () -> service.samples());

        assertEquals(ErrorCode.RECIPE_CANDIDATE_NOT_FOUND, e.getErrorCode());

    }

    private long count(MealSample sample, String category) {

        return sample.recipes().stream().filter(recipe -> category.equals(recipe.category())).count();

    }

}
