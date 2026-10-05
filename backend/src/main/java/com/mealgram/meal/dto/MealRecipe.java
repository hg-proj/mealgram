package com.mealgram.meal.dto;

// 식단안에 들어간 레시피 한 개

public record MealRecipe(Long id, String name, String category, String imageUrl) {

}
