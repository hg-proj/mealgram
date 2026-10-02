package com.mealgram.ingredient.dto;

import jakarta.validation.constraints.NotNull;

// 내 재료 등록 요청 데이터

public record MyIngredientRequest(@NotNull Long ingredientId) {

}
