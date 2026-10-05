package com.mealgram.recipe.dto;

import java.math.BigDecimal;
import java.util.List;

// 식단 후보로 쓸 레시피 검색 결과 데이터

public record RecipeCandidate(Long id,
                              String name,
                              String category,
                              String cookingMethod,
                              BigDecimal calorie,
                              BigDecimal carbohydrate,
                              BigDecimal protein,
                              BigDecimal fat,
                              BigDecimal sodium,
                              List<String> ingredients,
                              double similarity) {

}
