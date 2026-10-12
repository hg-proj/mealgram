package com.mealgram.common.embedding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class EmbeddingCacheTest {

    private final Map<String, String> store = new HashMap<>();
    private ValueOperations<String, String> valueOperations;
    private EmbeddingCache cache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doAnswer(invocation -> {
            store.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        when(valueOperations.get(anyString())).thenAnswer(invocation -> store.get(invocation.<String>getArgument(0)));
        cache = new EmbeddingCache(redisTemplate, "test-model");

    }

    @Test
    @DisplayName("저장한 벡터를 같은 문장으로 그대로 꺼낸다.")
    void returnsSavedVector() {

        float[] vector = {0.1f, -0.25f, 3.5f, 1.0E-5f};

        cache.save("재료: 두부 / 목표: 다이어트", vector);

        assertArrayEquals(vector, cache.find("재료: 두부 / 목표: 다이어트").orElseThrow());

    }

    @Test
    @DisplayName("저장하지 않은 문장은 비어 있다.")
    void missReturnsEmpty() {

        assertTrue(cache.find("처음 보는 문장").isEmpty());

    }

    @Test
    @DisplayName("키에 모델 이름과 문장의 해시를 쓰고 원문은 넣지 않는다.")
    void keyUsesModelAndHash() {

        String key = cache.keyOf("재료: 두부");

        assertTrue(key.startsWith("embedding:test-model:"));
        assertEquals("embedding:test-model:".length() + 64, key.length());
        assertTrue(!key.contains("두부"));
        assertNotEquals(key, cache.keyOf("재료: 김치"));
        assertEquals(key, cache.keyOf("재료: 두부"));

    }

    @Test
    @DisplayName("24시간 동안 보관한다.")
    void keepsForOneDay() {

        cache.save("문장", new float[] {1f});

        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofHours(24)));

    }

    @Test
    @DisplayName("Redis 오류가 나도 예외 없이 캐시 없는 것처럼 동작한다.")
    void redisFailureIsIgnored() {

        when(valueOperations.get(anyString())).thenThrow(new RedisConnectionFailureException("연결 실패"));
        doThrow(new RedisConnectionFailureException("연결 실패")).when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        assertTrue(cache.find("문장").isEmpty());
        cache.save("문장", new float[] {1f});

    }

    @Test
    @DisplayName("값이 깨져 있으면 캐시에 없는 것으로 본다.")
    void corruptedValueIsMiss() {

        store.put(cache.keyOf("문장"), "abc,def");

        assertTrue(cache.find("문장").isEmpty());

    }

}
