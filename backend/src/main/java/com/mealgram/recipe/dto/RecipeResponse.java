package com.mealgram.recipe.dto;

import java.math.BigDecimal;
import java.util.List;

import com.mealgram.recipe.Recipe;

// 레시피 상세, 부족 재료 응답 데이터

public record RecipeResponse(Long id,
                             String name,
                             String category,
                             String cookingMethod,
                             BigDecimal calorie,
                             BigDecimal carbohydrate,
                             BigDecimal protein,
                             BigDecimal fat,
                             BigDecimal sodium,
                             String cookingSteps,
                             String imageUrl,
                             List<String> missingIngredients,
                             List<String> seasonings) {

    public static RecipeResponse from(Recipe recipe, List<String> missingIngredients, List<String> seasonings) {

        return new RecipeResponse(recipe.getId(), recipe.getName(), recipe.getCategory(), recipe.getCookingMethod(),
                recipe.getCalorie(), recipe.getCarbohydrate(), recipe.getProtein(), recipe.getFat(),
                recipe.getSodium(), recipe.getCookingSteps(), recipe.getImageUrl(), missingIngredients, seasonings);

    }

}
