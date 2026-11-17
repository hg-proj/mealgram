package com.mealgram.meal.dto;

import java.util.List;

import jakarta.validation.constraints.Size;

// 식단을 추천받을 때 고른 조건, 저장한 식단을 다시 볼 때 씀

public record MealContext(@Size(max = 20) String goal,
                          @Size(max = 20) String style,
                          @Size(max = 50) String genre,
                          @Size(max = 50) String requiredName,
                          @Size(max = 100) List<@Size(max = 50) String> ownedNames) {

}
