package com.mealgram.meal.dto;

import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 식단 추천 요청 데이터

public record MealRequest(Long requiredId,
                          @Size(max = 20) List<Long> subIds,
                          Long ingredientId,
                          @Pattern(regexp = "다이어트|벌크업|유지", message = "목표는 다이어트, 벌크업, 유지 중 하나여야 합니다.") String goal,
                          @Size(max = 50) String genre) {

}
