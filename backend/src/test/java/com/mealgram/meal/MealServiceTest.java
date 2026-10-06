package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestClientException;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.llm.LlmClient;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.recipe.RecipeSearchService;
import com.mealgram.recipe.dto.RecipeCandidate;
import com.mealgram.recipe.dto.RecipeSearchResult;

import tools.jackson.databind.json.JsonMapper;

class MealServiceTest {

    private RecipeSearchService searchService;
    private RecipeRepository recipeRepository;
    private LlmClient llmClient;
    private MealService mealService;

    private RecipeCandidate candidate(long id, String category) {

        return new RecipeCandidate(id, "요리" + id, category, "", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN,
                BigDecimal.TEN, BigDecimal.TEN, List.of(), 0.5, 0, true);

    }

    private Recipe recipe(long id, String category) {

        return Recipe.builder().id(id).name("요리" + id).category(category).calorie(BigDecimal.TEN)
                .carbohydrate(BigDecimal.TEN).protein(BigDecimal.TEN).fat(BigDecimal.TEN).build();

    }

    @BeforeEach
    void setUp() {

        List<RecipeCandidate> candidates = new ArrayList<>();
        List<Recipe> recipes = new ArrayList<>();
        long id = 1;
        for (String category : List.of("밥", "밥", "밥", "국&찌개", "국&찌개", "국&찌개")) {
            candidates.add(candidate(id, category));
            recipes.add(recipe(id++, category));
        }
        for (int i = 0; i < 9; i++) {
            candidates.add(candidate(id, "반찬"));
            recipes.add(recipe(id++, "반찬"));
        }

        searchService = mock(RecipeSearchService.class);
        recipeRepository = mock(RecipeRepository.class);
        llmClient = mock(LlmClient.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(searchService.search(any(), any(), any(), any(), any())).thenReturn(new RecipeSearchResult(candidates, false));
        when(recipeRepository.findAllById(any())).thenReturn(recipes);
        when(memberRepository.findById(any())).thenReturn(Optional.of(Member.builder().build()));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        mealService = new MealService(searchService, recipeRepository, memberRepository, llmClient, redisTemplate,
                JsonMapper.builder().build());

    }

    private MealResponse recommend() {

        return mealService.recommend(1L, null, List.of(), null, "유지", "한식");

    }

    @Test
    @DisplayName("LLM이 식단안을 하나만 주면 다시 요청하고 모자라면 직접 채워 3개로 만든다.")
    void fillsUpToThreeMeals() {

        when(llmClient.chat(anyString(), anyString())).thenReturn(
                "{\"meals\":[{\"style\":\"담백\",\"reason\":\"설명\",\"recipeIds\":[1,4,7,8,9]}]}");

        MealResponse response = recommend();

        assertEquals(3, response.candidates().size());
        assertTrue(response.candidates().stream().noneMatch(meal -> meal.recipes().isEmpty()));
        verify(llmClient, times(2)).chat(anyString(), anyString());

    }

    @Test
    @DisplayName("LLM이 3개를 주면 그대로 쓰고 다시 요청하지 않는다.")
    void keepsThreeMealsFromLlm() {

        when(llmClient.chat(anyString(), anyString())).thenReturn(
                "{\"meals\":[{\"style\":\"가\",\"reason\":\"설명\",\"recipeIds\":[1,4,7,8,9]},"
                        + "{\"style\":\"나\",\"reason\":\"설명\",\"recipeIds\":[2,5,10,11,12]},"
                        + "{\"style\":\"다\",\"reason\":\"설명\",\"recipeIds\":[3,6,13,14,15]}]}");

        MealResponse response = recommend();

        assertEquals(3, response.candidates().size());
        assertEquals("가", response.candidates().get(0).style());
        verify(llmClient, times(1)).chat(anyString(), anyString());

    }

    @Test
    @DisplayName("LLM 호출이 계속 실패하면 식단 생성 실패로 알린다.")
    void failsWhenLlmAlwaysFails() {

        when(llmClient.chat(anyString(), anyString())).thenThrow(new RestClientException("실패"));

        assertThrows(BusinessException.class, this::recommend);

    }

}
