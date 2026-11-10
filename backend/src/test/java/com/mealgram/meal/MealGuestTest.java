package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.mealgram.common.llm.LlmClient;
import com.mealgram.meal.dto.BodyInfo;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.meal.nutrition.EnergyCalculator;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;
import com.mealgram.member.MemberRepository;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.recipe.RecipeSearchService;
import com.mealgram.recipe.dto.RecipeCandidate;
import com.mealgram.recipe.dto.RecipeSearchResult;

import tools.jackson.databind.json.JsonMapper;

class MealGuestTest {

    private static final String THREE_MEALS = "{\"meals\":[{\"style\":\"가\",\"reason\":\"설명\",\"recipeIds\":[1,4,7,8,9]},"
            + "{\"style\":\"나\",\"reason\":\"설명\",\"recipeIds\":[2,5,10,11,12]},"
            + "{\"style\":\"다\",\"reason\":\"설명\",\"recipeIds\":[3,6,13,14,15]}]}";

    private MemberRepository memberRepository;
    private StringRedisTemplate redisTemplate;
    private MealService mealService;

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

        RecipeSearchService searchService = mock(RecipeSearchService.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        LlmClient llmClient = mock(LlmClient.class);
        memberRepository = mock(MemberRepository.class);
        redisTemplate = mock(StringRedisTemplate.class);

        when(searchService.search(any(), any(), any(), any(), any())).thenReturn(new RecipeSearchResult(candidates, false));
        when(recipeRepository.findAllById(any())).thenReturn(recipes);
        when(llmClient.chat(anyString(), anyString())).thenReturn(THREE_MEALS);

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

    @Test
    @DisplayName("비회원 추천은 식단 세 개를 돌려주고 결과 id는 비어 있다.")
    void guestGetsThreeMealsWithoutId() {

        MealResponse response = mealService.recommendForGuest(null, List.of(), null, "유지", "한식", null);

        assertEquals(3, response.candidates().size());
        assertNull(response.id());

    }

    @Test
    @DisplayName("비회원 결과는 저장하지 않고 회원 정보도 조회하지 않는다.")
    void guestResultIsNotStored() {

        mealService.recommendForGuest(null, List.of(), null, "유지", "한식", null);

        verifyNoInteractions(redisTemplate, memberRepository);

    }

    @Test
    @DisplayName("신체 정보를 보내면 그 값으로 목표 열량을 계산한다.")
    void usesBodyInfoForTargetCalorie() {

        BodyInfo body = new BodyInfo(LocalDate.now().minusYears(30), Gender.MALE, new BigDecimal("175"), new BigDecimal("70"), ActivityLevel.MODERATE);
        int expected = EnergyCalculator.mealCalorie(
                EnergyCalculator.dailyCalorie(30, Gender.MALE, new BigDecimal("175"), new BigDecimal("70"),
                        ActivityLevel.MODERATE), "유지");

        MealResponse response = mealService.recommendForGuest(null, List.of(), null, "유지", "한식", body);

        assertEquals(expected, response.candidates().get(0).nutrition().targetCalorie());

    }

    @Test
    @DisplayName("신체 정보를 건너뛰면 기본 열량으로 계산한다.")
    void usesDefaultCalorieWithoutBodyInfo() {

        int expected = EnergyCalculator.mealCalorie(EnergyCalculator.DEFAULT_DAILY_CALORIE, "유지");

        MealResponse response = mealService.recommendForGuest(null, List.of(), null, "유지", "한식", null);

        assertEquals(expected, response.candidates().get(0).nutrition().targetCalorie());

    }

    @Test
    @DisplayName("목표에 따라 같은 신체 정보도 다른 열량으로 계산한다.")
    void goalChangesTargetCalorie() {

        BodyInfo body = new BodyInfo(LocalDate.now().minusYears(30), Gender.MALE, new BigDecimal("175"), new BigDecimal("70"), ActivityLevel.MODERATE);

        int diet = mealService.recommendForGuest(null, List.of(), null, "다이어트", "한식", body).candidates().get(0)
                .nutrition().targetCalorie();
        int bulk = mealService.recommendForGuest(null, List.of(), null, "벌크업", "한식", body).candidates().get(0)
                .nutrition().targetCalorie();

        assertEquals(true, diet < bulk);

    }

}
