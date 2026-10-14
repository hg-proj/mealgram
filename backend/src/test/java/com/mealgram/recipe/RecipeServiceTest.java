package com.mealgram.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.MemberIngredientRepository;
import com.mealgram.recipe.dto.RecipeResponse;

class RecipeServiceTest {

    private RecipeRepository recipeRepository;
    private RecipeIngredientRepository recipeIngredientRepository;
    private MemberIngredientRepository memberIngredientRepository;
    private RecipeService service;

    @BeforeEach
    void setUp() {

        recipeRepository = mock(RecipeRepository.class);
        recipeIngredientRepository = mock(RecipeIngredientRepository.class);
        memberIngredientRepository = mock(MemberIngredientRepository.class);
        service = new RecipeService(recipeRepository, recipeIngredientRepository, memberIngredientRepository);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(Recipe.builder()
                .id(1L).name("두부조림").category("반찬").cookingMethod("조림")
                .calorie(new BigDecimal("120")).cookingSteps("1. 두부를 굽는다.").build()));

    }

    private RecipeIngredient link(Long id, String name, String category) {

        return RecipeIngredient.builder()
                .ingredient(Ingredient.builder().id(id).name(name).category(category).build())
                .build();

    }

    @Test
    @DisplayName("없는 레시피는 404 오류로 알린다.")
    void rejectsUnknownRecipe() {

        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.getDetail(99L, 1L));

        assertEquals(ErrorCode.RECIPE_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("내가 가진 재료는 부족 재료에서 뺀다.")
    void excludesOwnedIngredients() {

        when(recipeIngredientRepository.findByRecipeId(1L)).thenReturn(List.of(
                link(10L, "두부", "두류"), link(11L, "대파", "채소류")));
        when(memberIngredientRepository.findIngredientIdsByMemberId(1L)).thenReturn(List.of(10L));

        RecipeResponse response = service.getDetail(1L, 1L);

        assertEquals(List.of("대파"), response.missingIngredients());

    }

    @Test
    @DisplayName("양념은 부족 재료가 아니라 양념 목록으로 따로 보여준다.")
    void separatesSeasonings() {

        when(recipeIngredientRepository.findByRecipeId(1L)).thenReturn(List.of(
                link(10L, "두부", "두류"), link(20L, "간장", Ingredient.SEASONING_CATEGORY)));
        when(memberIngredientRepository.findIngredientIdsByMemberId(1L)).thenReturn(List.of());

        RecipeResponse response = service.getDetail(1L, 1L);

        assertEquals(List.of("두부"), response.missingIngredients());
        assertEquals(List.of("간장"), response.seasonings());

    }

    @Test
    @DisplayName("재료를 모두 가지고 있으면 부족 재료가 비어 있다.")
    void noMissingWhenAllOwned() {

        when(recipeIngredientRepository.findByRecipeId(1L)).thenReturn(List.of(link(10L, "두부", "두류")));
        when(memberIngredientRepository.findIngredientIdsByMemberId(1L)).thenReturn(List.of(10L));

        RecipeResponse response = service.getDetail(1L, 1L);

        assertTrue(response.missingIngredients().isEmpty());

    }

    @Test
    @DisplayName("레시피 기본 정보를 그대로 응답한다.")
    void returnsRecipeFields() {

        when(recipeIngredientRepository.findByRecipeId(1L)).thenReturn(List.of());
        when(memberIngredientRepository.findIngredientIdsByMemberId(1L)).thenReturn(List.of());

        RecipeResponse response = service.getDetail(1L, 1L);

        assertEquals("두부조림", response.name());
        assertEquals("반찬", response.category());
        assertEquals(new BigDecimal("120"), response.calorie());

    }

}
