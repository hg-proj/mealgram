package com.mealgram.meal.nutrition;

import java.math.BigDecimal;
import java.util.List;

// 식단안 영양 합계와 검증 결과

public record NutritionSummary(BigDecimal calorie, BigDecimal carbohydrate, BigDecimal protein, BigDecimal fat,
                               BigDecimal carbohydrateRatio, BigDecimal proteinRatio, BigDecimal fatRatio,
                               int targetCalorie, boolean verified, boolean passed, List<String> issues) {

}
