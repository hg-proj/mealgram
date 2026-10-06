package com.mealgram.meal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.llm.LlmClient;
import com.mealgram.meal.dto.MealCandidate;
import com.mealgram.meal.dto.MealRecipe;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.meal.nutrition.EnergyCalculator;
import com.mealgram.meal.nutrition.MealNutritionValidator;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.recipe.RecipeSearchService;
import com.mealgram.recipe.dto.RecipeCandidate;
import com.mealgram.recipe.dto.RecipeSearchResult;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// 식단 추천 서비스

@Service
public class MealService {

    private static final String KEY_PREFIX = "meal:";
    private static final Duration RESULT_TTL = Duration.ofHours(24);
    private static final int MAX_ATTEMPTS = 2;
    private static final String EXTRA_STYLE = "추가 식단";
    private static final String EXTRA_REASON = "후보 레시피로 밥, 국, 반찬을 구성한 식단입니다.";

    private final RecipeSearchService recipeSearchService;
    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;
    private final LlmClient llmClient;
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public MealService(RecipeSearchService recipeSearchService,
                       RecipeRepository recipeRepository,
                       MemberRepository memberRepository,
                       LlmClient llmClient,
                       StringRedisTemplate redisTemplate,
                       JsonMapper jsonMapper) {
        this.recipeSearchService = recipeSearchService;
        this.recipeRepository = recipeRepository;
        this.memberRepository = memberRepository;
        this.llmClient = llmClient;
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    public MealResponse recommend(Long memberId,
                                  Long requiredId,
                                  List<Long> subIds,
                                  Long mainIngredientId,
                                  String goal,
                                  String genre) {

        RecipeSearchResult found = recipeSearchService.search(requiredId, subIds, mainIngredientId, goal, genre);
        List<RecipeCandidate> candidates = found.candidates();
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.RECIPE_CANDIDATE_NOT_FOUND);
        }

        MealResponse response = new MealResponse(UUID.randomUUID().toString(), found.relaxed(),
                generate(candidates, goal, genre, targetCalorie(memberId, goal)));
        redisTemplate.opsForValue().set(KEY_PREFIX + response.id(), jsonMapper.writeValueAsString(response), RESULT_TTL);

        return response;

    }

    public MealResponse get(String id) {

        String json = redisTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            throw new BusinessException(ErrorCode.MEAL_NOT_FOUND);
        }

        return jsonMapper.readValue(json, MealResponse.class);

    }

    private int targetCalorie(Long memberId, String goal) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        int daily = EnergyCalculator.dailyCalorie(member.getAge(), member.getGender(), member.getHeight(),
                member.getWeight(), member.getActivityLevel());

        return EnergyCalculator.mealCalorie(daily, goal);

    }

    private List<MealCandidate> generate(List<RecipeCandidate> candidates, String goal, String genre,
                                         int targetCalorie) {

        Set<Long> candidateIds = candidates.stream().map(RecipeCandidate::id).collect(Collectors.toSet());
        Map<Long, Recipe> recipes = recipeRepository.findAllById(candidateIds).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));

        Set<Long> usedIds = new HashSet<>();
        List<MealCandidate> meals = new ArrayList<>();
        for (int attempt = 0; attempt < MAX_ATTEMPTS && meals.size() < MealPrompt.MEAL_COUNT; attempt++) {
            for (LlmMeal meal : requestPlan(candidates, goal, genre, targetCalorie)) {
                MealCandidate candidate = toMealCandidate(meal, candidates, recipes, usedIds, targetCalorie);
                if (!candidate.recipes().isEmpty() && meals.size() < MealPrompt.MEAL_COUNT) {
                    meals.add(candidate);
                }
            }
        }
        if (meals.isEmpty()) {
            throw new BusinessException(ErrorCode.MEAL_GENERATION_FAILED);
        }

        while (meals.size() < MealPrompt.MEAL_COUNT) {
            List<Long> composedIds = MealComposer.compose(List.of(), candidates, usedIds);
            MealCandidate extra = toMealCandidate(new LlmMeal(EXTRA_STYLE, EXTRA_REASON, composedIds), candidates, recipes,
                    usedIds, targetCalorie);
            if (extra.recipes().isEmpty()) {
                break;
            }
            meals.add(extra);
        }

        return meals;

    }

    private List<LlmMeal> requestPlan(List<RecipeCandidate> candidates, String goal, String genre, int targetCalorie) {

        try {
            String content = llmClient.chat(MealPrompt.system(), MealPrompt.user(candidates, goal, genre, targetCalorie));
            LlmPlan plan = jsonMapper.readValue(content, LlmPlan.class);

            return plan.meals() == null ? List.of() : plan.meals();
        } catch (RestClientException | JacksonException e) {
            return List.of();
        }

    }

    private MealCandidate toMealCandidate(LlmMeal meal,
                                          List<RecipeCandidate> candidates,
                                          Map<Long, Recipe> recipes,
                                          Set<Long> usedIds,
                                          int targetCalorie) {

        List<Long> pickedIds = meal.recipeIds().stream()
                .distinct()
                .filter(recipes::containsKey)
                .toList();
        if (pickedIds.isEmpty()) {
            return new MealCandidate(meal.style(), meal.reason(), List.of(), null);
        }
        List<Long> composedIds = MealComposer.compose(pickedIds, candidates, usedIds);
        usedIds.addAll(composedIds);
        List<Recipe> picked = composedIds.stream()
                .map(recipes::get)
                .filter(recipe -> recipe != null)
                .toList();
        double[] portions = MealNutritionValidator.portions(picked, targetCalorie);
        List<MealRecipe> mealRecipes = IntStream.range(0, picked.size())
                .mapToObj(i -> new MealRecipe(picked.get(i).getId(), picked.get(i).getName(), picked.get(i).getCategory(),
                        picked.get(i).getImageUrl(), portions[i]))
                .toList();

        return new MealCandidate(meal.style(), meal.reason(), mealRecipes,
                MealNutritionValidator.validate(picked, portions, targetCalorie));

    }

    private record LlmPlan(List<LlmMeal> meals) {

    }

    private record LlmMeal(String style, String reason, List<Long> recipeIds) {

    }

}
