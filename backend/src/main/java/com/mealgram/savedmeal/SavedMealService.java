package com.mealgram.savedmeal;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.meal.MealService;
import com.mealgram.meal.dto.MealCandidate;
import com.mealgram.meal.dto.MealContext;
import com.mealgram.meal.dto.MealDish;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.meal.nutrition.NutritionSummary;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;
import com.mealgram.savedmeal.dto.SavedMealCreateResponse;
import com.mealgram.savedmeal.dto.SavedMealRequest;
import com.mealgram.savedmeal.dto.SavedMealResponse;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// 내 식단 저장, 목록 조회, 삭제 서비스

@Service
public class SavedMealService {

    private static final TypeReference<List<MealDish>> DISHES = new TypeReference<>() {};

    private final SavedMealRepository savedMealRepository;
    private final MemberRepository memberRepository;
    private final MealService mealService;
    private final JsonMapper jsonMapper;

    public SavedMealService(SavedMealRepository savedMealRepository,
                            MemberRepository memberRepository,
                            MealService mealService,
                            JsonMapper jsonMapper) {
        this.savedMealRepository = savedMealRepository;
        this.memberRepository = memberRepository;
        this.mealService = mealService;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public SavedMealCreateResponse save(Long memberId, SavedMealRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MealResponse meal = mealService.get(memberId, request.mealId());
        if (request.candidateIndex() >= meal.candidates().size()) {
            throw new BusinessException(ErrorCode.MEAL_NOT_FOUND);
        }
        if (savedMealRepository.existsByMemberIdAndMealIdAndCandidateIndex(memberId, request.mealId(),
                request.candidateIndex())) {
            throw new BusinessException(ErrorCode.DUPLICATE_SAVED_MEAL);
        }

        MealCandidate candidate = meal.candidates().get(request.candidateIndex());
        try {
            SavedMeal savedMeal = savedMealRepository.save(SavedMeal.builder()
                    .member(member)
                    .mealId(request.mealId())
                    .candidateIndex(request.candidateIndex())
                    .style(candidate.style())
                    .dishesJson(jsonMapper.writeValueAsString(candidate.dishes()))
                    .nutritionJson(jsonMapper.writeValueAsString(candidate.nutrition()))
                    .contextJson(request.context() == null ? null : jsonMapper.writeValueAsString(request.context()))
                    .build());

            return new SavedMealCreateResponse(savedMeal.getId());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_SAVED_MEAL);
        }

    }

    @Transactional(readOnly = true)
    public List<SavedMealResponse> getSavedMeals(Long memberId, Pageable pageable) {

        return savedMealRepository.findByMemberId(memberId, pageable).stream()
                .map(this::toResponse)
                .toList();

    }

    @Transactional
    public void delete(Long memberId, Long savedMealId) {

        SavedMeal savedMeal = savedMealRepository.findByIdAndMemberId(savedMealId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SAVED_MEAL_NOT_FOUND));
        savedMealRepository.delete(savedMeal);

    }

    private SavedMealResponse toResponse(SavedMeal savedMeal) {

        return new SavedMealResponse(savedMeal.getId(), savedMeal.getStyle(),
                jsonMapper.readValue(savedMeal.getDishesJson(), DISHES),
                jsonMapper.readValue(savedMeal.getNutritionJson(), NutritionSummary.class),
                savedMeal.getContextJson() == null ? null
                        : jsonMapper.readValue(savedMeal.getContextJson(), MealContext.class),
                savedMeal.getCreatedAt());

    }

}
