package com.mealgram.meal.dto;

import java.util.List;

import com.mealgram.meal.nutrition.NutritionSummary;

// 식단안 한 개 데이터, 목표는 이 식단을 맞춘 목표, 장르는 든 요리의 장르

public record MealCandidate(String style,
                            List<MealDish> dishes,
                            NutritionSummary nutrition,
                            String goal,
                            String genre) {

    public MealCandidate(String style, List<MealDish> dishes, NutritionSummary nutrition, String goal) {

        this(style, dishes, nutrition, goal, null);

    }

    public MealCandidate(String style, List<MealDish> dishes, NutritionSummary nutrition) {

        this(style, dishes, nutrition, null, null);

    }

}
