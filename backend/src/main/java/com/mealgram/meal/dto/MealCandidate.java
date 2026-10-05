package com.mealgram.meal.dto;

import java.util.List;

// 식단안 한 개 데이터

public record MealCandidate(String style, String reason, List<MealRecipe> recipes) {

}
