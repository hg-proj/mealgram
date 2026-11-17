package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.exception.ExternalApiException;
import com.mealgram.common.llm.LlmClient;
import com.mealgram.dish.DishPhotos;
import com.mealgram.dish.DishRole;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.meal.dto.BodyInfo;
import com.mealgram.meal.dto.MealCandidate;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.member.Member;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;
import com.mealgram.member.MemberRepository;

import tools.jackson.databind.json.JsonMapper;

class MealServiceTest {

    private DishComposer dishComposer;
    private DishPhotos dishPhotos;
    private IngredientRepository ingredientRepository;
    private MemberRepository memberRepository;
    private LlmClient llmClient;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private MealService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        dishComposer = mock(DishComposer.class);
        dishPhotos = mock(DishPhotos.class);
        ingredientRepository = mock(IngredientRepository.class);
        memberRepository = mock(MemberRepository.class);
        llmClient = mock(LlmClient.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new MealService(dishComposer, dishPhotos, ingredientRepository, memberRepository, llmClient,
                redisTemplate, JsonMapper.builder().build());

    }

    private ComposedMeal meal(boolean requiredMet, String mainName) {

        return DishFixtures.meal(requiredMet,
                DishFixtures.dish("쌀밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.dish(mainName, DishRole.MAIN, 250, 20, 12, 15));

    }

    private List<ComposedMeal> candidates(boolean requiredMet) {

        return List.of(meal(requiredMet, "가"), meal(requiredMet, "나"), meal(requiredMet, "다"), meal(requiredMet, "라"));

    }

    private void givenCandidates(List<ComposedMeal> meals) {

        when(dishComposer.compose(any(MealCondition.class), anyInt(), any())).thenReturn(meals);

    }

    private Member member() {

        return Member.builder().id(1L).birthDate(LocalDate.now().minusYears(30)).gender(Gender.MALE)
                .height(new BigDecimal("175")).weight(new BigDecimal("70")).activityLevel(ActivityLevel.MODERATE).build();

    }

    @Test
    @DisplayName("AI가 고른 순서와 이름, 이유로 식단 세 개를 만든다.")
    void usesLlmPick() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenReturn("""
                {"meals":[{"index":2,"style":"가벼운 한 상"},
                          {"index":0,"style":"든든한 한 상"},
                          {"index":3,"style":"균형 한 상"}]}""");

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        assertEquals(3, response.candidates().size());
        assertEquals("가벼운 한 상", response.candidates().get(0).style());
        assertEquals("다", response.candidates().get(0).dishes().get(1).name());
        assertEquals("가", response.candidates().get(1).dishes().get(1).name());
        assertFalse(response.relaxed());
        assertNull(response.id());

    }

    @Test
    @DisplayName("AI 호출이 실패하면 앞의 후보를 기본 이름으로 쓴다.")
    void fallsBackWhenLlmFails() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "상태 500"));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        assertEquals(3, response.candidates().size());
        assertEquals("가", response.candidates().get(0).dishes().get(1).name());
        assertEquals("나", response.candidates().get(1).dishes().get(1).name());

    }

    @Test
    @DisplayName("AI 키가 없어 호출할 수 없어도 앞의 후보로 식단 세 개를 만든다.")
    void fallsBackWhenLlmUnavailable() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new IllegalStateException("OPENAI_API_KEY 환경변수가 필요합니다."));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        assertEquals(3, response.candidates().size());

    }

    @Test
    @DisplayName("세 식단의 메인이 불고기끼리나 같은 재료끼리 겹치지 않게 고른다.")
    void picksDistinctMainFamilies() {

        givenCandidates(List.of(meal(true, "소불고기"), meal(true, "콩나물불고기"), meal(true, "고등어구이"),
                meal(true, "고등어조림"), meal(true, "두부조림")));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        List<String> mains = response.candidates().stream().map(candidate -> candidate.dishes().get(1).name()).toList();
        assertEquals(List.of("소불고기", "고등어구이", "두부조림"), mains);

    }

    @Test
    @DisplayName("같은 사진을 쓰는 후보는 세 식단에 겹치지 않게 고른다.")
    void picksDistinctPhotos() {

        ComposedMeal first = DishFixtures.meal(true, DishFixtures.dish("쌀밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("닭다리오븐구이", "p1"));
        ComposedMeal sameA = DishFixtures.meal(true, DishFixtures.dish("잡곡밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("닭다리구이", "p1"));
        ComposedMeal sameB = DishFixtures.meal(true, DishFixtures.dish("보리밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("닭다리조림", "p1"));
        ComposedMeal otherA = DishFixtures.meal(false, DishFixtures.dish("현미밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("두부조림", "p2"));
        ComposedMeal otherB = DishFixtures.meal(false, DishFixtures.dish("흑미밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("고등어구이", "p3"));
        givenCandidates(List.of(first, sameA, sameB, otherA, otherB));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        List<String> mains = response.candidates().stream().map(candidate -> candidate.dishes().get(1).name()).toList();
        assertEquals(List.of("닭다리오븐구이", "두부조림", "고등어구이"), mains);

    }

    @Test
    @DisplayName("사진이 겹치지 않는 후보가 모자라면 겹쳐도 세 개를 채운다.")
    void fillsWithSamePhotoWhenNotEnoughDistinct() {

        ComposedMeal first = DishFixtures.meal(true, DishFixtures.dish("쌀밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("닭다리오븐구이", "p1"));
        ComposedMeal second = DishFixtures.meal(true, DishFixtures.dish("잡곡밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("닭다리구이", "p1"));
        ComposedMeal third = DishFixtures.meal(true, DishFixtures.dish("보리밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.mainWithPhoto("닭다리조림", "p1"));
        givenCandidates(List.of(first, second, third));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        assertEquals(3, response.candidates().size());

    }

    @Test
    @DisplayName("영양 기준을 벗어난 후보는 뒤로 밀어 기준을 지킨 후보를 먼저 고른다.")
    void prefersCandidatesThatPassNutrition() {

        ComposedMeal fatty = DishFixtures.meal(true,
                DishFixtures.dish("쌀밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.dish("기름진요리", DishRole.MAIN, 250, 5, 40, 10));
        givenCandidates(List.of(fatty, meal(true, "가"), meal(true, "고등어구이"), meal(true, "두부조림")));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        List<String> mains = response.candidates().stream().map(candidate -> candidate.dishes().get(1).name()).toList();
        assertEquals(List.of("가", "고등어구이", "두부조림"), mains);

    }

    @Test
    @DisplayName("겹치지 않는 후보가 모자라면 겹쳐도 세 개를 채운다.")
    void fillsWithSimilarWhenNotEnoughDistinct() {

        givenCandidates(List.of(meal(true, "소불고기"), meal(true, "콩나물불고기"), meal(true, "언양식불고기"), meal(true, "고등어구이")));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        assertEquals(3, response.candidates().size());

    }

    @Test
    @DisplayName("AI 응답이 JSON이 아니거나 번호가 잘못돼도 남는 후보로 채운다.")
    void ignoresBrokenLlmPlan() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString()))
                .thenReturn("not json")
                .thenReturn("""
                        {"meals":[{"index":9,"style":"x"},{"index":1,"style":"x"},{"index":1,"style":"z"}]}""");

        MealResponse broken = service.recommendForGuest(null, null, null, "유지", null, null, null);
        MealResponse invalid = service.recommendForGuest(null, null, null, "유지", null, null, null);

        assertEquals(3, broken.candidates().size());
        assertEquals(3, invalid.candidates().size());
        assertEquals("나", invalid.candidates().get(0).dishes().get(1).name());
        assertEquals("가", invalid.candidates().get(1).dishes().get(1).name());
        assertEquals("다", invalid.candidates().get(2).dishes().get(1).name());

    }

    @Test
    @DisplayName("후보 중 필수 재료를 못 지킨 식단이 있으면 조건을 풀었다고 표시한다.")
    void marksRelaxed() {

        givenCandidates(candidates(false));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "시간 초과"));

        MealResponse response = service.recommendForGuest(null, null, null, "유지", null, null, null);

        assertTrue(response.relaxed());

    }

    @Test
    @DisplayName("구성할 수 있는 식단이 없으면 예외가 난다.")
    void throwsWhenNoCandidate() {

        givenCandidates(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.recommendForGuest(null, null, null, "유지", null, null, null));

        assertEquals(ErrorCode.MEAL_CANDIDATE_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("없는 재료를 고르면 예외가 난다.")
    void throwsWhenIngredientMissing() {

        when(ingredientRepository.findAllById(any())).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.recommendForGuest(5L, null, null, "유지", null, null, null));

        assertEquals(ErrorCode.INGREDIENT_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("선택한 재료 이름을 구성 조건에 넘긴다.")
    void passesIngredientNames() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));
        when(ingredientRepository.findAllById(any())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return ids.stream().map(id -> Ingredient.builder().id(id).name("재료" + id).build()).toList();
        });

        service.recommendForGuest(1L, List.of(2L, 3L), null, "벌크업", null, "고단백", null);

        ArgumentCaptor<MealCondition> captor = ArgumentCaptor.forClass(MealCondition.class);
        verify(dishComposer).compose(captor.capture(), anyInt(), any());
        assertEquals(List.of("재료1"), captor.getValue().requiredNames());
        assertEquals(List.of("재료2", "재료3"), captor.getValue().preferredNames());
        assertEquals(DietStyle.HIGH_PROTEIN, captor.getValue().style());
        assertEquals("벌크업", captor.getValue().goal());

    }

    @Test
    @DisplayName("회원 추천은 결과를 24시간 동안 저장하고 본인만 다시 볼 수 있다.")
    void storesMemberResult() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member()));

        MealResponse response = service.recommend(1L, null, null, null, "유지", null, null, null);

        assertNotNull(response.id());
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("meal:" + response.id()), json.capture(), eq(Duration.ofHours(24)));

        when(valueOperations.get("meal:" + response.id())).thenReturn(json.getValue());
        assertEquals(response.id(), service.get(1L, response.id()).id());
        BusinessException other = assertThrows(BusinessException.class, () -> service.get(2L, response.id()));
        assertEquals(ErrorCode.MEAL_NOT_FOUND, other.getErrorCode());

    }

    @Test
    @DisplayName("목표가 랜덤이면 후보마다 서로 다른 목표를 붙인다.")
    void randomGoalGivesEachCandidateOwnGoal() {

        givenCandidates(candidates(true));

        MealResponse response = service.recommendForGuest(null, null, null, "랜덤", null, null, null);

        assertEquals(Set.of("다이어트", "유지", "벌크업"),
                response.candidates().stream().map(candidate -> candidate.goal()).collect(Collectors.toSet()));
        assertEquals(3, response.candidates().size());

    }

    @Test
    @DisplayName("쉼표로 이은 장르를 나누어 구성 조건에 넣고 랜덤은 뺀다.")
    void splitsGenresIntoCondition() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

        service.recommendForGuest(null, null, null, "유지", "한식, 양식, 랜덤", null, null);

        ArgumentCaptor<MealCondition> captor = ArgumentCaptor.forClass(MealCondition.class);
        verify(dishComposer).compose(captor.capture(), anyInt(), any());
        assertEquals(List.of("한식", "양식"), captor.getValue().genres());

    }

    @Test
    @DisplayName("공유 결과는 올린 사람이 없어서 어느 회원이든 다시 볼 수 있다.")
    void sharedResultIsReadableByAnyMember() {

        MealResponse stored = service.storeShared(List.of(new MealCandidate("이름", List.of(), null, "유지")));

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("meal:" + stored.id()), json.capture(), eq(Duration.ofHours(24)));
        when(valueOperations.get("meal:" + stored.id())).thenReturn(json.getValue());

        assertEquals(stored.id(), service.get(1L, stored.id()).id());
        assertEquals(stored.id(), service.get(2L, stored.id()).id());

    }

    @Test
    @DisplayName("비회원 추천은 결과를 저장하지 않는다.")
    void guestIsNotStored() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));

        service.recommendForGuest(null, null, null, "유지", null, null, null);

        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));

    }

    @Test
    @DisplayName("저장된 결과가 없거나 깨져 있으면 찾을 수 없다고 한다.")
    void getFailsWhenMissingOrBroken() {

        when(valueOperations.get("meal:none")).thenReturn(null);
        when(valueOperations.get("meal:broken")).thenReturn("{{");

        assertEquals(ErrorCode.MEAL_NOT_FOUND,
                assertThrows(BusinessException.class, () -> service.get(1L, "none")).getErrorCode());
        assertEquals(ErrorCode.MEAL_NOT_FOUND,
                assertThrows(BusinessException.class, () -> service.get(1L, "broken")).getErrorCode());

    }

    @Test
    @DisplayName("없는 회원이면 예외가 난다.")
    void throwsWhenMemberMissing() {

        when(memberRepository.findById(9L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.recommend(9L, null, null, null, "유지", null, null, null));

        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("요청에 신체 정보가 있으면 내 정보 대신 그 값으로 목표 열량을 정한다.")
    void bodyInfoOverridesProfile() {

        givenCandidates(candidates(true));
        when(llmClient.chat(anyString(), anyString())).thenThrow(new ExternalApiException("LLM", "x"));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member()));
        BodyInfo small = new BodyInfo(LocalDate.now().minusYears(70), Gender.FEMALE, new BigDecimal("150"),
                new BigDecimal("45"), ActivityLevel.LOW);

        service.recommend(1L, null, null, null, "유지", null, null, null);
        service.recommend(1L, null, null, null, "유지", null, null, small);

        ArgumentCaptor<MealCondition> captor = ArgumentCaptor.forClass(MealCondition.class);
        verify(dishComposer, org.mockito.Mockito.times(2)).compose(captor.capture(), anyInt(), any());
        assertTrue(captor.getAllValues().get(1).targetCalorie() < captor.getAllValues().get(0).targetCalorie());

    }

}
