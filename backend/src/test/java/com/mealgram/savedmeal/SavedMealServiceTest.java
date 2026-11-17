package com.mealgram.savedmeal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

import tools.jackson.databind.json.JsonMapper;

class SavedMealServiceTest {

    private static final String MEAL_ID = "11111111-1111-1111-1111-111111111111";

    private SavedMealRepository savedMealRepository;
    private MemberRepository memberRepository;
    private MealService mealService;
    private JsonMapper jsonMapper;
    private SavedMealService savedMealService;
    private Member member;

    @BeforeEach
    void setUp() {

        savedMealRepository = mock(SavedMealRepository.class);
        memberRepository = mock(MemberRepository.class);
        mealService = mock(MealService.class);
        jsonMapper = JsonMapper.builder().build();
        savedMealService = new SavedMealService(savedMealRepository, memberRepository, mealService, jsonMapper);
        member = Member.builder().id(1L).build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(mealService.get(1L, MEAL_ID)).thenReturn(new MealResponse(MEAL_ID, false, List.of(candidate("담백한 한 상"),
                candidate("든든한 한 상"))));

    }

    private MealCandidate candidate(String style) {

        return new MealCandidate(style,
                List.of(new MealDish(1L, "쌀밥", "밥", null, 210, 300, List.of("쌀"), List.of()), new MealDish(2L, "미역국", "국", null, 200, 40, List.of("미역"), List.of())),
                new NutritionSummary(new BigDecimal("340.0"), new BigDecimal("70.0"), new BigDecimal("10.0"),
                        new BigDecimal("2.0"), new BigDecimal("80.0"), new BigDecimal("12.0"), new BigDecimal("8.0"), 500,
                        true, true, List.of()));

    }

    @Test
    @DisplayName("추천받은 식단 한 상을 그대로 내 식단에 저장한다.")
    void savesCandidate() {

        when(savedMealRepository.existsByMemberIdAndMealIdAndCandidateIndex(1L, MEAL_ID, 1)).thenReturn(false);
        when(savedMealRepository.save(any(SavedMeal.class)))
                .thenReturn(SavedMeal.builder().id(5L).member(member).build());

        SavedMealCreateResponse response = savedMealService.save(1L, new SavedMealRequest(MEAL_ID, 1));

        ArgumentCaptor<SavedMeal> captor = ArgumentCaptor.forClass(SavedMeal.class);
        verify(savedMealRepository).save(captor.capture());
        assertEquals(5L, response.id());
        assertEquals("든든한 한 상", captor.getValue().getStyle());
        assertEquals(1, captor.getValue().getCandidateIndex());
        assertEquals(true, captor.getValue().getDishesJson().contains("미역국"));

    }

    @Test
    @DisplayName("저장할 때 보낸 추천 조건을 함께 저장하고, 없으면 비워 둔다.")
    void savesContext() {

        when(savedMealRepository.existsByMemberIdAndMealIdAndCandidateIndex(1L, MEAL_ID, 0)).thenReturn(false);
        when(savedMealRepository.save(any(SavedMeal.class)))
                .thenReturn(SavedMeal.builder().id(5L).member(member).build());

        savedMealService.save(1L, new SavedMealRequest(MEAL_ID, 0,
                new MealContext("유지", "균형", null, "꽃게", List.of("양파"))));
        savedMealService.save(1L, new SavedMealRequest(MEAL_ID, 0));

        ArgumentCaptor<SavedMeal> captor = ArgumentCaptor.forClass(SavedMeal.class);
        verify(savedMealRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(true, captor.getAllValues().get(0).getContextJson().contains("꽃게"));
        assertEquals(null, captor.getAllValues().get(1).getContextJson());

    }

    @Test
    @DisplayName("이미 저장한 식단은 다시 저장할 수 없다.")
    void rejectsDuplicate() {

        when(savedMealRepository.existsByMemberIdAndMealIdAndCandidateIndex(1L, MEAL_ID, 0)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> savedMealService.save(1L, new SavedMealRequest(MEAL_ID, 0)));

        assertEquals(ErrorCode.DUPLICATE_SAVED_MEAL, exception.getErrorCode());
        verify(savedMealRepository, never()).save(any());

    }

    @Test
    @DisplayName("동시에 저장해 유일 제약에 걸려도 이미 저장한 식단으로 알린다.")
    void rejectsConcurrentDuplicate() {

        when(savedMealRepository.save(any(SavedMeal.class))).thenThrow(new DataIntegrityViolationException("중복"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> savedMealService.save(1L, new SavedMealRequest(MEAL_ID, 0)));

        assertEquals(ErrorCode.DUPLICATE_SAVED_MEAL, exception.getErrorCode());

    }

    @Test
    @DisplayName("추천 결과가 없거나 내 것이 아니면 저장할 수 없다.")
    void rejectsUnknownMeal() {

        when(mealService.get(1L, "other")).thenThrow(new BusinessException(ErrorCode.MEAL_NOT_FOUND));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> savedMealService.save(1L, new SavedMealRequest("other", 0)));

        assertEquals(ErrorCode.MEAL_NOT_FOUND, exception.getErrorCode());
        verify(savedMealRepository, never()).save(any());

    }

    @Test
    @DisplayName("결과에 없는 번호의 식단은 저장할 수 없다.")
    void rejectsIndexOutOfRange() {

        BusinessException exception = assertThrows(BusinessException.class,
                () -> savedMealService.save(1L, new SavedMealRequest(MEAL_ID, 2)));

        assertEquals(ErrorCode.MEAL_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("없는 회원은 저장할 수 없다.")
    void rejectsUnknownMember() {

        when(memberRepository.findById(9L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> savedMealService.save(9L, new SavedMealRequest(MEAL_ID, 0)));

        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("내 식단 목록은 저장한 요리와 영양을 되살려 돌려준다.")
    void returnsSavedMeals() {

        SavedMeal saved = SavedMeal.builder().id(7L).member(member).mealId(MEAL_ID).candidateIndex(0).style("담백한 한 상")
                .dishesJson("[{\"id\":1,\"name\":\"쌀밥\",\"category\":\"밥\",\"imageUrl\":null,\"grams\":210,\"calorie\":300}]")
                .nutritionJson(jsonMapper.writeValueAsString(candidate("x").nutrition()))
                .createdAt(LocalDateTime.of(2026, 11, 10, 12, 0)).build();
        PageRequest pageable = PageRequest.of(0, 10);
        when(savedMealRepository.findByMemberId(1L, pageable)).thenReturn(new PageImpl<>(List.of(saved), pageable, 1));

        List<SavedMealResponse> responses = savedMealService.getSavedMeals(1L, pageable);

        assertEquals(1, responses.size());
        assertEquals("담백한 한 상", responses.get(0).style());
        assertEquals("쌀밥", responses.get(0).dishes().get(0).name());
        assertEquals(500, responses.get(0).nutrition().targetCalorie());

    }

    @Test
    @DisplayName("내 식단만 삭제할 수 있다.")
    void deletesOnlyMine() {

        SavedMeal saved = SavedMeal.builder().id(7L).member(member).build();
        when(savedMealRepository.findByIdAndMemberId(7L, 1L)).thenReturn(Optional.of(saved));
        when(savedMealRepository.findByIdAndMemberId(7L, 2L)).thenReturn(Optional.empty());

        savedMealService.delete(1L, 7L);

        verify(savedMealRepository).delete(saved);
        BusinessException exception = assertThrows(BusinessException.class, () -> savedMealService.delete(2L, 7L));
        assertEquals(ErrorCode.SAVED_MEAL_NOT_FOUND, exception.getErrorCode());

    }


}
