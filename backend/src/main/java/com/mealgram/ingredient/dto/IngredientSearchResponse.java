package com.mealgram.ingredient.dto;

import com.mealgram.ingredient.Ingredient;

// 전체 재료 검색 응답 데이터

public record IngredientSearchResponse(Long id, String name) {

    public static IngredientSearchResponse from(Ingredient ingredient) {

        return new IngredientSearchResponse(ingredient.getId(), ingredient.getName());
        
    }

    
}
