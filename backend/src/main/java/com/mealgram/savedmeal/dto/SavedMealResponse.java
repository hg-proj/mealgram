package com.mealgram.savedmeal.dto;

import com.mealgram.savedmeal.SavedMeal;

// 내 식단 목록 응답 데이터

public record SavedMealResponse(Long id, Long recipeId, String name) {

    public static SavedMealResponse from(SavedMeal savedMeal) {

        return new SavedMealResponse(savedMeal.getId(), savedMeal.getRecipe().getId(), savedMeal.getRecipe().getName());

    }

}
