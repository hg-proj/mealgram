package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
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

class MealResultOwnerTest {

    private static final String THREE_MEALS = "{\"meals\":[{\"style\":\"가\",\"reason\":\"설명\",\"recipeIds\":[1,4,7,8,9]},"
            + "{\"style\":\"나\",\"reason\":\"설명\",\"recipeIds\":[2,5,10,11,12]},"
            + "{\"style\":\"다\",\"reason\":\"설명\",\"recipeIds\":[3,6,13,14,15]}]}";

    private final Map<String, String> store = new HashMap<>();
    private MealService mealService;

    @BeforeEach
    @SuppressWarnings("unchecked")
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

        RecipeSearchService searchService = mock(RecipeSearchService.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        LlmClient llmClient = mock(LlmClient.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(searchService.search(any(), any(), any(), any(), any())).thenReturn(new RecipeSearchResult(candidates, false));
        when(recipeRepository.findAllById(any())).thenReturn(recipes);
        when(memberRepository.findById(any())).thenReturn(Optional.of(Member.builder().build()));
        when(llmClient.chat(anyString(), anyString())).thenReturn(THREE_MEALS);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doAnswer(invocation -> {
            store.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(valueOperations.get(anyString())).thenAnswer(invocation -> store.get(invocation.<String>getArgument(0)));

        mealService = new MealService(searchService, recipeRepository, memberRepository, llmClient, redisTemplate,
                JsonMapper.builder().build());

    }

    private RecipeCandidate candidate(long id, String category) {

        return new RecipeCandidate(id, "요리" + id, category, "", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN,
                BigDecimal.TEN, BigDecimal.TEN, List.of(), 0.5, 0, true);

    }

    private Recipe recipe(long id, String category) {

        return Recipe.builder().id(id).name("요리" + id).category(category).calorie(BigDecimal.TEN)
                .carbohydrate(BigDecimal.TEN).protein(BigDecimal.TEN).fat(BigDecimal.TEN).build();

    }

    private void assertNotFound(Long memberId, String id) {

        BusinessException e = assertThrows(BusinessException.class, () -> mealService.get(memberId, id));

        assertEquals(ErrorCode.MEAL_NOT_FOUND, e.getErrorCode());

    }

    @Test
    @DisplayName("추천 결과를 만든 회원은 다시 조회할 수 있다.")
    void ownerCanReadResult() {

        MealResponse created = mealService.recommend(1L, null, List.of(), null, "유지", "한식");

        MealResponse found = mealService.get(1L, created.id());

        assertEquals(created.id(), found.id());
        assertEquals(3, found.candidates().size());

    }

    @Test
    @DisplayName("다른 회원은 결과가 있어도 없는 결과와 같은 404로 응답한다.")
    void otherMemberGetsNotFound() {

        MealResponse created = mealService.recommend(1L, null, List.of(), null, "유지", "한식");

        assertNotFound(2L, created.id());

    }

    @Test
    @DisplayName("회원 표시가 없는 결과는 조회할 수 없다.")
    void resultWithoutOwnerIsNotFound() {

        MealResponse created = mealService.recommend(1L, null, List.of(), null, "유지", "한식");
        store.remove("meal:owner:" + created.id());

        assertNotFound(1L, created.id());

    }

    @Test
    @DisplayName("없는 id는 404로 응답한다.")
    void unknownIdIsNotFound() {

        assertNotFound(1L, "nothing");

    }

    @Test
    @DisplayName("결과가 만료되면 회원 표시가 남아 있어도 404로 응답한다.")
    void expiredResultIsNotFound() {

        MealResponse created = mealService.recommend(1L, null, List.of(), null, "유지", "한식");
        store.remove("meal:" + created.id());

        assertNotFound(1L, created.id());

    }

}
