package com.mealgram.recipe;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.MemberIngredientRepository;
import com.mealgram.recipe.dto.RecipeResponse;

// 레시피 상세 조회, 부족 재료 계산 처리 서비스

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final MemberIngredientRepository memberIngredientRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository recipeIngredientRepository,
                         MemberIngredientRepository memberIngredientRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.memberIngredientRepository = memberIngredientRepository;
    }

    @Transactional(readOnly = true)
    public RecipeResponse getDetail(Long recipeId, Long memberId) {

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));

        List<Ingredient> ingredients = recipeIngredientRepository.findByRecipeId(recipeId).stream()
                .map(RecipeIngredient::getIngredient)
                .toList();
        Set<Long> ownedIds = new HashSet<>(memberIngredientRepository.findIngredientIdsByMemberId(memberId));

        List<String> seasonings = ingredients.stream()
                .filter(this::isSeasoning)
                .map(Ingredient::getName)
                .toList();
        List<String> missingIngredients = ingredients.stream()
                .filter(ingredient -> !isSeasoning(ingredient) && !ownedIds.contains(ingredient.getId()))
                .map(Ingredient::getName)
                .toList();

        return RecipeResponse.from(recipe, missingIngredients, seasonings);

    }

    private boolean isSeasoning(Ingredient ingredient) {

        return Ingredient.SEASONING_CATEGORY.equals(ingredient.getCategory());

    }

}
