package com.mealgram.meal.dto;

import java.util.List;

import jakarta.validation.constraints.Size;

// 식단 추천 요청 데이터

public record MealRequest(Long requiredId,
                          @Size(max = 20) List<Long> subIds,
                          Long ingredientId,
                          @Size(max = 50) String goal,
                          @Size(max = 50) String genre) {

}
