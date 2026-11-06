package com.mealgram.meal.dto;

import java.util.List;

import com.mealgram.meal.nutrition.NutritionSummary;

// 랜덤 식단 한 개 데이터

public record MealSample(String goal, String title, List<MealRecipe> recipes, NutritionSummary nutrition) {

}
