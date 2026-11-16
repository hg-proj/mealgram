package com.mealgram.ingredient.dto;

import java.util.List;

// 분류별 선택 가능 재료 응답 데이터

public record IngredientCatalogResponse(String category, List<IngredientSearchResponse> ingredients) {

}
