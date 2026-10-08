package com.mealgram.savedmeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.savedmeal.dto.SavedMealCreateResponse;
import com.mealgram.savedmeal.dto.SavedMealRequest;
import com.mealgram.savedmeal.dto.SavedMealResponse;

class SavedMealServiceTest {

    private SavedMealRepository savedMealRepository;
    private MemberRepository memberRepository;
    private RecipeRepository recipeRepository;
    private SavedMealService savedMealService;
    private Member member;
    private Recipe recipe;

    @BeforeEach
    void setUp() {

        savedMealRepository = mock(SavedMealRepository.class);
        memberRepository = mock(MemberRepository.class);
        recipeRepository = mock(RecipeRepository.class);
        savedMealService = new SavedMealService(savedMealRepository, memberRepository, recipeRepository);
        member = Member.builder().id(1L).build();
        recipe = Recipe.builder().id(10L).name("샤브샤브").build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));

    }

    @Test
    @DisplayName("레시피를 내 식단에 저장한다.")
    void savesRecipe() {

        when(savedMealRepository.existsByMemberIdAndRecipeId(1L, 10L)).thenReturn(false);
        when(savedMealRepository.save(any(SavedMeal.class)))
                .thenReturn(SavedMeal.builder().id(5L).member(member).recipe(recipe).build());

        SavedMealCreateResponse response = savedMealService.save(1L, new SavedMealRequest(10L));

        assertEquals(5L, response.id());

    }

    @Test
    @DisplayName("이미 저장한 레시피는 다시 저장할 수 없다.")
    void rejectsDuplicate() {

        when(savedMealRepository.existsByMemberIdAndRecipeId(1L, 10L)).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> savedMealService.save(1L, new SavedMealRequest(10L)));

        assertEquals(ErrorCode.DUPLICATE_SAVED_MEAL, e.getErrorCode());
        verify(savedMealRepository, never()).save(any());

    }

    @Test
    @DisplayName("동시에 저장해서 유니크 제약에 걸려도 중복 오류로 알린다.")
    void convertsUniqueViolationToDuplicate() {

        when(savedMealRepository.existsByMemberIdAndRecipeId(1L, 10L)).thenReturn(false);
        when(savedMealRepository.save(any(SavedMeal.class))).thenThrow(new DataIntegrityViolationException("중복"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> savedMealService.save(1L, new SavedMealRequest(10L)));

        assertEquals(ErrorCode.DUPLICATE_SAVED_MEAL, e.getErrorCode());

    }

    @Test
    @DisplayName("없는 레시피는 저장할 수 없다.")
    void rejectsMissingRecipe() {

        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException e = assertThrows(BusinessException.class,
                () -> savedMealService.save(1L, new SavedMealRequest(99L)));

        assertEquals(ErrorCode.RECIPE_NOT_FOUND, e.getErrorCode());

    }

    @Test
    @DisplayName("내 식단 목록은 저장 번호, 레시피 번호, 이름을 준다.")
    void listsSavedMeals() {

        SavedMeal saved = SavedMeal.builder().id(5L).member(member).recipe(recipe).build();
        PageRequest pageable = PageRequest.of(0, 20);
        when(savedMealRepository.findByMemberId(1L, pageable)).thenReturn(new PageImpl<>(List.of(saved)));

        List<SavedMealResponse> result = savedMealService.getSavedMeals(1L, pageable);

        assertEquals(1, result.size());
        assertEquals(new SavedMealResponse(5L, 10L, "샤브샤브"), result.get(0));

    }

    @Test
    @DisplayName("내 식단만 삭제할 수 있다.")
    void deletesOnlyMine() {

        SavedMeal saved = SavedMeal.builder().id(5L).member(member).recipe(recipe).build();
        when(savedMealRepository.findByIdAndMemberId(5L, 1L)).thenReturn(Optional.of(saved));
        when(savedMealRepository.findByIdAndMemberId(5L, 2L)).thenReturn(Optional.empty());

        savedMealService.delete(1L, 5L);
        verify(savedMealRepository).delete(saved);

        BusinessException e = assertThrows(BusinessException.class, () -> savedMealService.delete(2L, 5L));
        assertEquals(ErrorCode.SAVED_MEAL_NOT_FOUND, e.getErrorCode());

    }

}
