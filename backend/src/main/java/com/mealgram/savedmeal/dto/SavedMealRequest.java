package com.mealgram.savedmeal.dto;

import jakarta.validation.constraints.NotNull;

// 식단 저장 요청 데이터

public record SavedMealRequest(@NotNull Long recipeId) {

}
