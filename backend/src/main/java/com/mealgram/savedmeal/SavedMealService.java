package com.mealgram.savedmeal;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.savedmeal.dto.SavedMealCreateResponse;
import com.mealgram.savedmeal.dto.SavedMealRequest;
import com.mealgram.savedmeal.dto.SavedMealResponse;

// 내 식단 저장, 목록 조회, 삭제 서비스

@Service
public class SavedMealService {

    private final SavedMealRepository savedMealRepository;
    private final MemberRepository memberRepository;
    private final RecipeRepository recipeRepository;

    public SavedMealService(SavedMealRepository savedMealRepository,
                            MemberRepository memberRepository,
                            RecipeRepository recipeRepository) {
        this.savedMealRepository = savedMealRepository;
        this.memberRepository = memberRepository;
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public SavedMealCreateResponse save(Long memberId, SavedMealRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Recipe recipe = recipeRepository.findById(request.recipeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RECIPE_NOT_FOUND));

        if (savedMealRepository.existsByMemberIdAndRecipeId(memberId, recipe.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_SAVED_MEAL);
        }

        try {
            SavedMeal savedMeal = savedMealRepository.save(SavedMeal.builder().member(member).recipe(recipe).build());

            return new SavedMealCreateResponse(savedMeal.getId());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_SAVED_MEAL);
        }

    }

    @Transactional(readOnly = true)
    public List<SavedMealResponse> getSavedMeals(Long memberId, Pageable pageable) {

        return savedMealRepository.findByMemberId(memberId, pageable).stream()
                .map(SavedMealResponse::from)
                .toList();

    }

    @Transactional
    public void delete(Long memberId, Long savedMealId) {

        SavedMeal savedMeal = savedMealRepository.findByIdAndMemberId(savedMealId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SAVED_MEAL_NOT_FOUND));

        savedMealRepository.delete(savedMeal);

    }

}
