package com.mealgram.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.mealgram.common.embedding.EmbeddingCache;
import com.mealgram.common.embedding.EmbeddingClient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.recipe.dto.RecipeCandidate;

class RecipeSearchServiceTest {

    private final Map<String, String> store = new HashMap<>();
    private EmbeddingClient embeddingClient;
    private RecipeSearchService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doAnswer(invocation -> {
            store.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(valueOperations.get(anyString())).thenAnswer(invocation -> store.get(invocation.<String>getArgument(0)));

        embeddingClient = mock(EmbeddingClient.class);
        when(embeddingClient.embed(anyList())).thenReturn(List.of(new float[] {0.1f, 0.2f}));

        List<RecipeCandidate> candidates = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String category = i <= 3 ? "밥" : i <= 6 ? "국&찌개" : "반찬";
            candidates.add(new RecipeCandidate((long) i, "요리" + i, category, "", BigDecimal.TEN, BigDecimal.TEN,
                    BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, List.of(), 0.9 - i * 0.01, 0, false));
        }
        RecipeVectorRepository vectorRepository = mock(RecipeVectorRepository.class);
        when(vectorRepository.findSimilar(any(), anyList(), anyInt(), anyList(), anyInt())).thenReturn(candidates);

        service = new RecipeSearchService(vectorRepository, mock(IngredientRepository.class), embeddingClient,
                new EmbeddingCache(redisTemplate, "test-model"));

    }

    @Test
    @DisplayName("같은 조건으로 두 번 검색하면 임베딩은 한 번만 호출한다.")
    void embedsSameQueryOnce() {

        service.search(null, null, null, "다이어트", "한식");
        service.search(null, null, null, "다이어트", "한식");

        verify(embeddingClient, times(1)).embed(anyList());

    }

    @Test
    @DisplayName("조건이 다르면 임베딩을 다시 호출한다.")
    void embedsDifferentQueryAgain() {

        service.search(null, null, null, "다이어트", "한식");
        service.search(null, null, null, "벌크업", "한식");

        verify(embeddingClient, times(2)).embed(anyList());

    }

    @Test
    @DisplayName("캐시를 써도 검색 결과는 같다.")
    void sameResultWithCache() {

        List<Long> first = service.search(null, null, null, "다이어트", "한식").candidates().stream().map(RecipeCandidate::id).toList();
        List<Long> second = service.search(null, null, null, "다이어트", "한식").candidates().stream().map(RecipeCandidate::id).toList();

        assertEquals(first, second);

    }

}
