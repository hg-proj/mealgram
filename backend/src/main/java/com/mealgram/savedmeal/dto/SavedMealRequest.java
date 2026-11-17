package com.mealgram.savedmeal.dto;

import com.mealgram.meal.dto.MealContext;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 식단 저장 요청 데이터, 추천 조건은 선택

public record SavedMealRequest(@NotBlank @Size(max = 36) String mealId,
                               @NotNull @Min(0) @Max(2) Integer candidateIndex,
                               @Valid MealContext context) {

    public SavedMealRequest(String mealId, Integer candidateIndex) {

        this(mealId, candidateIndex, null);

    }

}
