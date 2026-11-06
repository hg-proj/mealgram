package com.mealgram.meal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.meal.dto.MealRecipe;
import com.mealgram.meal.dto.MealSample;
import com.mealgram.meal.nutrition.EnergyCalculator;
import com.mealgram.meal.nutrition.MealNutritionValidator;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.recipe.RecipeVectorRepository;
import com.mealgram.recipe.dto.RecipeCandidate;

// 랜덤 식단 서비스

@Service
public class MealSampleService {

    private static final List<String> GOALS = List.of("다이어트", "유지", "벌크업");
    private static final Map<String, String> TITLES = Map.of(
            "다이어트", "가벼운 한 그릇 식단",
            "유지", "든든한 한 끼 식단",
            "벌크업", "근육을 위한 식단");
    private static final int POOL_SIZE = 12;

    private final RecipeVectorRepository recipeVectorRepository;
    private final RecipeRepository recipeRepository;

    public MealSampleService(RecipeVectorRepository recipeVectorRepository, RecipeRepository recipeRepository) {
        this.recipeVectorRepository = recipeVectorRepository;
        this.recipeRepository = recipeRepository;
    }

    public List<MealSample> samples() {

        List<RecipeCandidate> mains = recipeVectorRepository.findRandomWithImage(RecipeCandidate.PROTEIN_DISH_MIN,
                GOALS.size());
        if (mains.size() < GOALS.size()) {
            throw new BusinessException(ErrorCode.RECIPE_CANDIDATE_NOT_FOUND);
        }

        List<RecipeCandidate> pool = pool(mains);
        Set<Long> usedIds = mains.stream().map(RecipeCandidate::id).collect(Collectors.toCollection(HashSet::new));
        List<List<Long>> composed = new ArrayList<>();
        for (RecipeCandidate main : mains) {
            List<Long> ids = MealComposer.compose(List.of(main.id()), pool, usedIds);
            usedIds.addAll(ids);
            composed.add(ids);
        }

        Map<Long, Recipe> recipes = recipeRepository.findAllById(usedIds).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));

        return IntStream.range(0, GOALS.size())
                .mapToObj(i -> toSample(GOALS.get(i), composed.get(i), recipes))
                .toList();

    }

    private List<RecipeCandidate> pool(List<RecipeCandidate> mains) {

        List<RecipeCandidate> candidates = new ArrayList<>(mains);
        candidates.addAll(recipeVectorRepository.findByCategory(null, "밥", POOL_SIZE));
        candidates.addAll(recipeVectorRepository.findByCategory(null, "국&찌개", POOL_SIZE));
        candidates.addAll(recipeVectorRepository.findProteinDishes(null, "반찬", RecipeCandidate.PROTEIN_DISH_MIN,
                POOL_SIZE));
        candidates.addAll(recipeVectorRepository.findByCategory(null, "반찬", POOL_SIZE));

        Map<Long, RecipeCandidate> unique = new LinkedHashMap<>();
        candidates.forEach(candidate -> unique.putIfAbsent(candidate.id(), candidate));

        return new ArrayList<>(unique.values());

    }

    private MealSample toSample(String goal, List<Long> ids, Map<Long, Recipe> recipes) {

        List<Recipe> picked = ids.stream()
                .map(recipes::get)
                .filter(recipe -> recipe != null)
                .toList();
        int targetCalorie = EnergyCalculator.mealCalorie(EnergyCalculator.DEFAULT_DAILY_CALORIE, goal);
        double[] portions = MealNutritionValidator.portions(picked, targetCalorie);
        List<MealRecipe> mealRecipes = IntStream.range(0, picked.size())
                .mapToObj(i -> new MealRecipe(picked.get(i).getId(), picked.get(i).getName(), picked.get(i).getCategory(),
                        picked.get(i).getImageUrl(), portions[i]))
                .toList();

        return new MealSample(goal, TITLES.get(goal), mealRecipes,
                MealNutritionValidator.validate(picked, portions, targetCalorie));

    }

}
