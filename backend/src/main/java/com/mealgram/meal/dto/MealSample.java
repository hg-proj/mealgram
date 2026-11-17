package com.mealgram.meal.dto;

import java.util.List;

import com.mealgram.meal.nutrition.NutritionSummary;

// 랜덤 식단 한 개 데이터, id로 저장할 수 있음

public record MealSample(String id,
                         String goal,
                         String genre,
                         String title,
                         List<MealDish> dishes,
                         NutritionSummary nutrition) {

}
