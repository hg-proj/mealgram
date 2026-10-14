package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.llm.LlmClient;
import com.mealgram.member.MemberRepository;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.recipe.RecipeSearchService;

import tools.jackson.databind.json.JsonMapper;

class MealMemberCheckTest {

    private RecipeSearchService searchService;
    private LlmClient llmClient;
    private MemberRepository memberRepository;
    private MealService mealService;

    @BeforeEach
    void setUp() {

        searchService = mock(RecipeSearchService.class);
        llmClient = mock(LlmClient.class);
        memberRepository = mock(MemberRepository.class);
        mealService = new MealService(searchService, mock(RecipeRepository.class), memberRepository, llmClient,
                mock(StringRedisTemplate.class), JsonMapper.builder().build());

    }

    @Test
    @DisplayName("없는 회원은 검색과 외부 호출 없이 404 오류로 알린다.")
    void unknownMemberFailsBeforeAnyExternalCall() {

        when(memberRepository.findById(9L)).thenReturn(Optional.empty());

        BusinessException e = assertThrows(BusinessException.class,
                () -> mealService.recommend(9L, null, List.of(), null, "다이어트", "한식"));

        assertEquals(ErrorCode.MEMBER_NOT_FOUND, e.getErrorCode());
        verifyNoInteractions(searchService, llmClient);

    }

}
