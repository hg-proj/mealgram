package com.mealgram.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class LoginAttemptStoreTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private LoginAttemptStore store;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new LoginAttemptStore(redisTemplate);

    }

    @Test
    @DisplayName("실패 기록이 없으면 막지 않는다.")
    void notBlockedWithoutFailures() {

        when(valueOperations.get("login:fail:hangyeol01")).thenReturn(null);

        assertFalse(store.isBlocked("hangyeol01"));

    }

    @Test
    @DisplayName("실패가 5회가 되기 전까지는 막지 않고 5회부터 막는다.")
    void blocksFromFifthFailure() {

        when(valueOperations.get("login:fail:hangyeol01")).thenReturn("4");
        assertFalse(store.isBlocked("hangyeol01"));

        when(valueOperations.get("login:fail:hangyeol01")).thenReturn("5");
        assertTrue(store.isBlocked("hangyeol01"));

        when(valueOperations.get("login:fail:hangyeol01")).thenReturn("12");
        assertTrue(store.isBlocked("hangyeol01"));

    }

    @Test
    @DisplayName("처음 실패하면 15분 만료를 설정한다.")
    void firstFailureSetsExpiry() {

        when(valueOperations.increment("login:fail:hangyeol01")).thenReturn(1L);

        store.recordFailure("hangyeol01");

        verify(redisTemplate).expire("login:fail:hangyeol01", Duration.ofMinutes(15));

    }

    @Test
    @DisplayName("이미 만료가 있으면 실패할 때마다 만료를 늘리지 않는다.")
    void laterFailureKeepsExpiry() {

        when(valueOperations.increment("login:fail:hangyeol01")).thenReturn(3L);
        when(redisTemplate.getExpire("login:fail:hangyeol01")).thenReturn(600L);

        store.recordFailure("hangyeol01");

        verify(redisTemplate, never()).expire("login:fail:hangyeol01", Duration.ofMinutes(15));

    }

    @Test
    @DisplayName("만료가 없는 채로 남은 키는 다시 만료를 설정한다.")
    void repairsKeyWithoutExpiry() {

        when(valueOperations.increment("login:fail:hangyeol01")).thenReturn(4L);
        when(redisTemplate.getExpire("login:fail:hangyeol01")).thenReturn(-1L);

        store.recordFailure("hangyeol01");

        verify(redisTemplate).expire("login:fail:hangyeol01", Duration.ofMinutes(15));

    }

    @Test
    @DisplayName("성공하면 실패 기록을 지운다.")
    void resetDeletesKey() {

        store.reset("hangyeol01");

        verify(redisTemplate).delete("login:fail:hangyeol01");

    }

}
