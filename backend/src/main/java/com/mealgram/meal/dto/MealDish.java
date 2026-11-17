package com.mealgram.meal.dto;

import java.util.List;

// 식단에 들어간 요리 한 개와 양

public record MealDish(Long id, String name, String category, String imageUrl, int grams, int calorie,
                       List<String> ingredients,
                       List<String> seasonings) {

}
