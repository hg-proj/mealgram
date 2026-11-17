package com.mealgram.meal;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.exception.ExternalApiException;
import com.mealgram.common.llm.LlmClient;
import com.mealgram.dish.Dish;
import com.mealgram.dish.DishPhotos;
import com.mealgram.dish.DishRole;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.meal.dto.BodyInfo;
import com.mealgram.meal.dto.MealCandidate;
import com.mealgram.meal.dto.MealDish;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.meal.nutrition.EnergyCalculator;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// 식단 추천 서비스

@Slf4j
@Service
public class MealService {

    private static final String KEY_PREFIX = "meal:";
    private static final Duration RESULT_TTL = Duration.ofHours(24);
    private static final int CANDIDATE_COUNT = 9;
    private static final List<String> DEFAULT_STYLES = List.of("담백한 한식 한 상", "든든한 한식 한 상", "균형 잡힌 한식 한 상");
    private static final String RANDOM_GOAL = "랜덤";
    private static final List<String> GOALS = List.of("다이어트", "유지", "벌크업");
    private static final int RANDOM_CANDIDATE_COUNT = 3;
    private static final String GENRE_SEPARATOR = ",";

    private final DishComposer dishComposer;
    private final DishPhotos dishPhotos;
    private final IngredientRepository ingredientRepository;
    private final MemberRepository memberRepository;
    private final LlmClient llmClient;
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public MealService(DishComposer dishComposer,
                       DishPhotos dishPhotos,
                       IngredientRepository ingredientRepository,
                       MemberRepository memberRepository,
                       LlmClient llmClient,
                       StringRedisTemplate redisTemplate,
                       JsonMapper jsonMapper) {
        this.dishComposer = dishComposer;
        this.dishPhotos = dishPhotos;
        this.ingredientRepository = ingredientRepository;
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
                                  String genre,
                                  String style,
                                  BodyInfo body) {

        MealResponse response = compose(UUID.randomUUID().toString(), "회원 " + memberId, requiredId, subIds,
                mainIngredientId, goal, genre, style, dailyCalorie(memberId, body));
        redisTemplate.opsForValue().set(KEY_PREFIX + response.id(),
                jsonMapper.writeValueAsString(new StoredMeal(memberId, response)), RESULT_TTL);

        return response;

    }

    public MealResponse recommendForGuest(Long requiredId,
                                          List<Long> subIds,
                                          Long mainIngredientId,
                                          String goal,
                                          String genre,
                                          String style,
                                          BodyInfo body) {

        return compose(null, "비회원", requiredId, subIds, mainIngredientId, goal, genre, style, dailyCalorie(body));

    }

    public MealResponse storeShared(List<MealCandidate> candidates) {

        MealResponse response = new MealResponse(UUID.randomUUID().toString(), false, candidates);
        redisTemplate.opsForValue().set(KEY_PREFIX + response.id(),
                jsonMapper.writeValueAsString(new StoredMeal(null, response)), RESULT_TTL);

        return response;

    }

    private MealResponse compose(String id,
                                 String owner,
                                 Long requiredId,
                                 List<Long> subIds,
                                 Long mainIngredientId,
                                 String goal,
                                 String genre,
                                 String style,
                                 int dailyCalorie) {

        if (RANDOM_GOAL.equals(goal)) {
            return composeRandomGoals(id, owner, requiredId, subIds, mainIngredientId, genre, style, dailyCalorie);
        }

        return composeFor(id, owner, requiredId, subIds, mainIngredientId, goal, genre, style,
                EnergyCalculator.mealCalorie(dailyCalorie, goal));

    }

    private MealResponse composeRandomGoals(String id,
                                            String owner,
                                            Long requiredId,
                                            List<Long> subIds,
                                            Long mainIngredientId,
                                            String genre,
                                            String style,
                                            int dailyCalorie) {

        long start = System.currentTimeMillis();
        DietStyle dietStyle = DietStyle.from(style);
        List<String> goals = new ArrayList<>(GOALS);
        Collections.shuffle(goals, ThreadLocalRandom.current());
        List<MealCandidate> meals = new ArrayList<>();
        Set<String> usedPhotos = new HashSet<>();
        boolean relaxed = false;
        for (int i = 0; i < goals.size(); i++) {
            String goal = goals.get(i);
            int targetCalorie = EnergyCalculator.mealCalorie(dailyCalorie, goal);
            List<ComposedMeal> composed = passedFirst(dishComposer.compose(
                    condition(requiredId, subIds, mainIngredientId, goal, genre, dietStyle, targetCalorie),
                    RANDOM_CANDIDATE_COUNT, ThreadLocalRandom.current()), targetCalorie, dietStyle);
            if (composed.isEmpty()) {
                throw new BusinessException(ErrorCode.MEAL_CANDIDATE_NOT_FOUND);
            }
            ComposedMeal first = composed.stream()
                    .filter(meal -> meal.photoGroups().stream().noneMatch(usedPhotos::contains))
                    .findFirst()
                    .orElse(composed.get(0));
            usedPhotos.addAll(first.photoGroups());
            relaxed = relaxed || !first.requiredMet();
            meals.add(toMealCandidate(first, DEFAULT_STYLES.get(i), dietStyle, targetCalorie,
                    goal, genres(genre)));
        }

        log.info("식단 추천 완료: {}, 목표 무작위, {}ms", owner, System.currentTimeMillis() - start);

        return new MealResponse(id, relaxed, meals);

    }

    private MealCondition condition(Long requiredId,
                                    List<Long> subIds,
                                    Long mainIngredientId,
                                    String goal,
                                    String genre,
                                    DietStyle dietStyle,
                                    int targetCalorie) {

        return new MealCondition(goal, dietStyle, targetCalorie,
                ingredientNames(requiredId == null ? List.of() : List.of(requiredId), mainIngredientId),
                ingredientNames(subIds == null ? List.of() : subIds, null), genres(genre));

    }

    private static List<String> genres(String genre) {

        if (genre == null || genre.isBlank()) {
            return List.of();
        }

        return Arrays.stream(genre.split(GENRE_SEPARATOR))
                .map(String::strip)
                .filter(name -> !name.isEmpty() && !RANDOM_GOAL.equals(name))
                .toList();

    }

    private MealResponse composeFor(String id,
                                    String owner,
                                    Long requiredId,
                                    List<Long> subIds,
                                    Long mainIngredientId,
                                    String goal,
                                    String genre,
                                    String style,
                                    int targetCalorie) {

        long start = System.currentTimeMillis();
        DietStyle dietStyle = DietStyle.from(style);
        MealCondition condition = condition(requiredId, subIds, mainIngredientId, goal, genre, dietStyle, targetCalorie);
        List<ComposedMeal> composed = passedFirst(
                dishComposer.compose(condition, CANDIDATE_COUNT, ThreadLocalRandom.current()), targetCalorie, dietStyle);
        if (composed.isEmpty()) {
            throw new BusinessException(ErrorCode.MEAL_CANDIDATE_NOT_FOUND);
        }
        boolean relaxed = composed.stream().anyMatch(meal -> !meal.requiredMet());

        List<MealCandidate> meals = select(composed, goal, genre, dietStyle, targetCalorie);

        log.info("식단 추천 완료: {}, 후보 {}개, 조건 완화 {}, {}ms", owner, composed.size(), relaxed,
                System.currentTimeMillis() - start);

        return new MealResponse(id, relaxed, meals);

    }

    private List<ComposedMeal> passedFirst(List<ComposedMeal> meals, int targetCalorie, DietStyle style) {

        List<ComposedMeal> sorted = new ArrayList<>(meals);
        sorted.sort(Comparator.comparing(meal -> !MealNutrition.summarize(meal, targetCalorie, style).passed()));

        return sorted;

    }

    private List<MealCandidate> select(List<ComposedMeal> composed,
                                       String goal,
                                       String genre,
                                       DietStyle style,
                                       int targetCalorie) {

        List<LlmMeal> plan = requestPlan(composed, goal, genre, style, targetCalorie);
        List<String> chosenGenres = genres(genre);
        List<MealCandidate> meals = new ArrayList<>();
        Set<Integer> picked = new HashSet<>();
        Used used = new Used();
        for (LlmMeal meal : plan) {
            if (meals.size() < MealPrompt.MEAL_COUNT && meal.index() != null && meal.index() >= 0
                    && meal.index() < composed.size() && !picked.contains(meal.index())
                    && used.accept(composed.get(meal.index()), Pick.FAMILY_AND_PHOTO)) {
                picked.add(meal.index());
                meals.add(toMealCandidate(composed.get(meal.index()), text(meal.style(), DEFAULT_STYLES.get(meals.size())),
                        style, targetCalorie, goal, chosenGenres));
            }
        }
        fill(meals, composed, picked, used, style, targetCalorie, goal, chosenGenres, Pick.FAMILY_AND_PHOTO);
        fill(meals, composed, picked, used, style, targetCalorie, goal, chosenGenres, Pick.PHOTO);
        fill(meals, composed, picked, used, style, targetCalorie, goal, chosenGenres, Pick.ANY);

        return meals;

    }

    private void fill(List<MealCandidate> meals,
                      List<ComposedMeal> composed,
                      Set<Integer> picked,
                      Used used,
                      DietStyle style,
                      int targetCalorie,
                      String goal,
                      List<String> chosenGenres,
                      Pick pick) {

        for (int i = 0; i < composed.size() && meals.size() < MealPrompt.MEAL_COUNT; i++) {
            if (!picked.contains(i) && used.accept(composed.get(i), pick)) {
                picked.add(i);
                meals.add(toMealCandidate(composed.get(i), DEFAULT_STYLES.get(meals.size()), style,
                        targetCalorie, goal, chosenGenres));
            }
        }

    }

    private enum Pick {
        FAMILY_AND_PHOTO, PHOTO, ANY
    }

    private static final class Used {

        private final Set<String> families = new HashSet<>();
        private final Set<String> photos = new HashSet<>();

        boolean accept(ComposedMeal meal, Pick pick) {

            Set<String> ownFamilies = meal.mainFamilies();
            Set<String> ownPhotos = meal.photoGroups();
            boolean familyFree = ownFamilies.stream().noneMatch(families::contains);
            boolean photoFree = ownPhotos.stream().noneMatch(photos::contains);
            boolean ok = switch (pick) {
                case FAMILY_AND_PHOTO -> familyFree && photoFree;
                case PHOTO -> photoFree;
                case ANY -> true;
            };
            if (ok) {
                families.addAll(ownFamilies);
                photos.addAll(ownPhotos);
            }

            return ok;

        }

    }

    private List<LlmMeal> requestPlan(List<ComposedMeal> composed, String goal, String genre, DietStyle style,
                                      int targetCalorie) {

        try {
            String content = llmClient.chat(MealPrompt.system(), MealPrompt.user(composed, goal, style, genre, targetCalorie));
            LlmPlan plan = jsonMapper.readValue(content, LlmPlan.class);

            return plan.meals() == null ? List.of() : plan.meals();
        } catch (ExternalApiException e) {
            return List.of();
        } catch (IllegalStateException e) {
            log.warn("LLM을 쓸 수 없어 규칙 기반 후보를 그대로 씁니다: {}", e.getMessage());
            return List.of();
        } catch (JacksonException e) {
            log.warn("LLM 응답을 읽지 못했습니다: {}", e.getClass().getSimpleName());
            return List.of();
        }

    }

    private MealCandidate toMealCandidate(ComposedMeal meal, String style, DietStyle dietStyle,
                                          int targetCalorie, String goal, List<String> chosenGenres) {

        List<MealDish> dishes = meal.dishes().stream()
                .map(composed -> new MealDish(composed.dish().getId(), composed.dish().getName(),
                        composed.dish().getRole().getLabel(), dishPhotos.urlOf(composed.dish()), composed.grams(),
                        (int) Math.round(composed.calorie()), composed.dish().ingredientList(),
                        composed.dish().seasoningList()))
                .toList();

        return new MealCandidate(style, dishes, MealNutrition.summarize(meal, targetCalorie, dietStyle), goal,
                MealGenre.of(meal.dishes(), chosenGenres));

    }

    private List<String> ingredientNames(List<Long> ids, Long extraId) {

        List<Long> all = new ArrayList<>(ids);
        if (extraId != null) {
            all.add(extraId);
        }
        if (all.isEmpty()) {
            return List.of();
        }
        List<Ingredient> found = ingredientRepository.findAllById(all);
        if (found.size() != new HashSet<>(all).size()) {
            throw new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND);
        }

        return found.stream().map(Ingredient::getName).toList();

    }

    private static String text(String value, String fallback) {

        return value == null || value.isBlank() ? fallback : value.strip();

    }

    private static int dailyCalorie(BodyInfo body) {

        if (body == null) {
            return EnergyCalculator.DEFAULT_DAILY_CALORIE;
        }

        return EnergyCalculator.dailyCalorie(body.ageOn(LocalDate.now()), body.gender(), body.height(), body.weight(),
                body.activityLevel());

    }

    public MealResponse get(Long memberId, String id) {

        String json = redisTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            throw new BusinessException(ErrorCode.MEAL_NOT_FOUND);
        }

        StoredMeal stored = readStored(json);
        if (stored == null || stored.response() == null
                || stored.memberId() != null && !stored.memberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEAL_NOT_FOUND);
        }

        return stored.response();

    }

    private StoredMeal readStored(String json) {

        try {
            return jsonMapper.readValue(json, StoredMeal.class);
        } catch (JacksonException e) {
            return null;
        }

    }

    private int dailyCalorie(Long memberId, BodyInfo body) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return body != null && body.hasAny()
                ? dailyCalorie(body)
                : EnergyCalculator.dailyCalorie(member.ageOn(LocalDate.now()), member.getGender(), member.getHeight(),
                        member.getWeight(), member.getActivityLevel());

    }

    private record StoredMeal(Long memberId, MealResponse response) {
    }

    private record LlmPlan(List<LlmMeal> meals) {

    }

    private record LlmMeal(Integer index, String style) {

    }

}
