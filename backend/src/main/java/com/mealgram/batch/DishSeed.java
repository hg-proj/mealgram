package com.mealgram.batch;

import java.math.BigDecimal;
import java.util.List;

// 요리 시드 데이터

public record DishSeed(String name,
                       String role,
                       String cuisine,
                       List<String> mainIngredients,
                       List<String> methods,
                       List<String> flags,
                       String dessertType,
                       String imageGroup,
                       boolean heavySoup,
                       int servingGram,
                       BigDecimal calorie,
                       BigDecimal protein,
                       BigDecimal fat,
                       BigDecimal carbohydrate,
                       String source,
                       int frequency,
                       List<String> pairings,
                       List<String> ingredients,
                       List<String> seasonings) {

}
