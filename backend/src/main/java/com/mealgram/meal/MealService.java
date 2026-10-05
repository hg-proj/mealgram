package com.mealgram.meal;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.llm.LlmClient;
import com.mealgram.meal.dto.MealCandidate;
import com.mealgram.meal.dto.MealRecipe;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;
import com.mealgram.recipe.RecipeSearchService;
import com.mealgram.recipe.dto.RecipeCandidate;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// 식단 추천 처리 서비스. 후보 검색, LLM 호출, 결과 저장과 재조회

@Service
public class MealService {

    private static final String KEY_PREFIX = "meal:";
    private static final Duration RESULT_TTL = Duration.ofHours(24);

    private final RecipeSearchService recipeSearchService;
    private final RecipeRepository recipeRepository;
    private final LlmClient llmClient;
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public MealService(RecipeSearchService recipeSearchService,
                       RecipeRepository recipeRepository,
                       LlmClient llmClient,
                       StringRedisTemplate redisTemplate,
                       JsonMapper jsonMapper) {
        this.recipeSearchService = recipeSearchService;
        this.recipeRepository = recipeRepository;
        this.llmClient = llmClient;
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    public MealResponse recommend(Long requiredId,
                                  List<Long> subIds,
                                  Long mainIngredientId,
                                  String goal,
                                  String genre) {

        List<RecipeCandidate> candidates = recipeSearchService.search(requiredId, subIds, mainIngredientId, goal, genre);
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.RECIPE_CANDIDATE_NOT_FOUND);
        }

        boolean relaxed = candidates.stream().anyMatch(candidate -> !candidate.matched());
        MealResponse response = new MealResponse(UUID.randomUUID().toString(), relaxed,
                generate(candidates, goal, genre));
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

    private List<MealCandidate> generate(List<RecipeCandidate> candidates, String goal, String genre) {

        LlmPlan plan;
        try {
            String content = llmClient.chat(MealPrompt.system(), MealPrompt.user(candidates, goal, genre));
            plan = jsonMapper.readValue(content, LlmPlan.class);
        } catch (RestClientException | JacksonException e) {
            throw new BusinessException(ErrorCode.MEAL_GENERATION_FAILED);
        }

        Set<Long> candidateIds = candidates.stream().map(RecipeCandidate::id).collect(Collectors.toSet());
        Map<Long, Recipe> recipes = recipeRepository.findAllById(candidateIds).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));

        List<MealCandidate> meals = plan.meals().stream()
                .map(meal -> toMealCandidate(meal, recipes))
                .filter(meal -> !meal.recipes().isEmpty())
                .toList();
        if (meals.isEmpty()) {
            throw new BusinessException(ErrorCode.MEAL_GENERATION_FAILED);
        }

        return meals;

    }

    private MealCandidate toMealCandidate(LlmMeal meal, Map<Long, Recipe> recipes) {

        List<MealRecipe> mealRecipes = meal.recipeIds().stream()
                .distinct()
                .map(recipes::get)
                .filter(recipe -> recipe != null)
                .map(recipe -> new MealRecipe(recipe.getId(), recipe.getName(), recipe.getCategory(), recipe.getImageUrl()))
                .toList();

        return new MealCandidate(meal.style(), meal.reason(), mealRecipes);

    }

    private record LlmPlan(List<LlmMeal> meals) {

    }

    private record LlmMeal(String style, String reason, List<Long> recipeIds) {

    }

}
