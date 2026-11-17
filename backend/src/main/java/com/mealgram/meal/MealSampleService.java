package com.mealgram.meal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.dish.DishPhotos;
import com.mealgram.meal.dto.MealCandidate;
import com.mealgram.meal.dto.MealDish;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.meal.dto.MealSample;
import com.mealgram.meal.nutrition.EnergyCalculator;

// 랜덤 식단 서비스

@Service
public class MealSampleService {

    private static final List<String> GOALS = List.of("다이어트", "유지", "벌크업");
    private static final Map<String, String> TITLES = Map.of(
            "다이어트", "가벼운 한 그릇 식단",
            "유지", "든든한 한 끼 식단",
            "벌크업", "근육을 위한 식단");
    private static final int CANDIDATE_COUNT = 6;

    private final DishComposer dishComposer;
    private final DishPhotos dishPhotos;
    private final MealService mealService;

    public MealSampleService(DishComposer dishComposer, DishPhotos dishPhotos, MealService mealService) {
        this.dishComposer = dishComposer;
        this.dishPhotos = dishPhotos;
        this.mealService = mealService;
    }

    public List<MealSample> samples() {

        return build();

    }

    private List<MealSample> build() {

        List<MealSample> samples = new ArrayList<>();
        for (String goal : GOALS) {
            samples.add(sample(goal));
        }

        return samples;

    }

    private MealSample sample(String goal) {

        int targetCalorie = EnergyCalculator.mealCalorie(EnergyCalculator.DEFAULT_DAILY_CALORIE, goal);
        MealCondition condition = new MealCondition(goal, DietStyle.BALANCED, targetCalorie, List.of(), List.of());
        List<ComposedMeal> meals = dishComposer.compose(condition, CANDIDATE_COUNT, ThreadLocalRandom.current());
        if (meals.isEmpty()) {
            throw new BusinessException(ErrorCode.MEAL_CANDIDATE_NOT_FOUND);
        }
        ComposedMeal meal = meals.stream()
                .filter(candidate -> candidate.dishes().stream().anyMatch(composed -> dishPhotos.urlOf(composed.dish()) != null))
                .findFirst()
                .orElse(meals.get(0));
        List<MealDish> dishes = meal.dishes().stream()
                .map(composed -> new MealDish(composed.dish().getId(), composed.dish().getName(),
                        composed.dish().getRole().getLabel(), dishPhotos.urlOf(composed.dish()), composed.grams(),
                        (int) Math.round(composed.calorie()), composed.dish().ingredientList(),
                        composed.dish().seasoningList()))
                .toList();

        MealCandidate candidate = new MealCandidate(TITLES.get(goal), dishes,
                MealNutrition.summarize(meal, targetCalorie, DietStyle.BALANCED), goal, MealGenre.of(meal.dishes()));
        MealResponse stored = mealService.storeShared(List.of(candidate));

        return new MealSample(stored.id(), goal, candidate.genre(), candidate.style(), candidate.dishes(),
                candidate.nutrition());

    }

}
