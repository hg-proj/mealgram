package com.mealgram.batch;

import java.math.BigDecimal;
import java.util.List;

// 레시피 시드 데이터

public record RecipeSeed(String name,
                         String category,
                         String cookingMethod,
                         BigDecimal calorie,
                         BigDecimal carbohydrate,
                         BigDecimal protein,
                         BigDecimal fat,
                         BigDecimal sodium,
                         String cookingSteps,
                         String imageUrl,
                         String ingredientRawText,
                         List<String> ingredients) {

}
