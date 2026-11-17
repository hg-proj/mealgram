package com.mealgram.savedmeal.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.mealgram.meal.dto.MealContext;
import com.mealgram.meal.dto.MealDish;
import com.mealgram.meal.nutrition.NutritionSummary;

// 내 식단 응답 데이터

public record SavedMealResponse(Long id,
                                String style,
                                List<MealDish> dishes,
                                NutritionSummary nutrition,
                                MealContext context,
                                LocalDateTime createdAt) {

}
