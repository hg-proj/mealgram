package com.mealgram.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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

class PasswordResetCodeStoreTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private PasswordResetCodeStore store;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new PasswordResetCodeStore(redisTemplate);

    }

    @Test
    @DisplayName("인증번호를 10분 동안 보관한다.")
    void savesForTenMinutes() {

        store.save(1L, "123456");

        verify(valueOperations).set("pwreset:code:1", "123456", Duration.ofMinutes(10));

    }

    @Test
    @DisplayName("맞는 인증번호는 확인하고 한 번만 쓰도록 지운다.")
    void consumesMatchingCode() {

        when(valueOperations.get("pwreset:code:1")).thenReturn("123456");

        assertTrue(store.consume(1L, "123456"));

        verify(redisTemplate).delete("pwreset:code:1");

    }

    @Test
    @DisplayName("틀린 인증번호는 지우지 않고 거절한다.")
    void rejectsWrongCode() {

        when(valueOperations.get("pwreset:code:1")).thenReturn("123456");

        assertFalse(store.consume(1L, "654321"));

        verify(redisTemplate, never()).delete(anyString());

    }

    @Test
    @DisplayName("보관한 인증번호가 없거나 만료되었으면 거절한다.")
    void rejectsMissingCode() {

        when(valueOperations.get("pwreset:code:1")).thenReturn(null);

        assertFalse(store.consume(1L, "123456"));

    }

    @Test
    @DisplayName("실패 기록이 없으면 막지 않는다.")
    void notBlockedWithoutFailures() {

        when(valueOperations.get("pwreset:fail:han@example.com")).thenReturn(null);

        assertFalse(store.isBlocked("han@example.com"));

    }

    @Test
    @DisplayName("틀린 횟수가 5회가 되기 전까지는 막지 않고 5회부터 막는다.")
    void blocksFromFifthFailure() {

        when(valueOperations.get("pwreset:fail:han@example.com")).thenReturn("4");
        assertFalse(store.isBlocked("han@example.com"));

        when(valueOperations.get("pwreset:fail:han@example.com")).thenReturn("5");
        assertTrue(store.isBlocked("han@example.com"));

    }

    @Test
    @DisplayName("처음 틀리면 10분 만료를 설정한다.")
    void firstFailureSetsExpiry() {

        when(valueOperations.increment("pwreset:fail:han@example.com")).thenReturn(1L);

        store.recordFailure("han@example.com");

        verify(redisTemplate).expire("pwreset:fail:han@example.com", Duration.ofMinutes(10));

    }

    @Test
    @DisplayName("이미 만료가 있으면 틀릴 때마다 만료를 늘리지 않는다.")
    void laterFailureKeepsExpiry() {

        when(valueOperations.increment("pwreset:fail:han@example.com")).thenReturn(3L);
        when(redisTemplate.getExpire("pwreset:fail:han@example.com")).thenReturn(300L);

        store.recordFailure("han@example.com");

        verify(redisTemplate, never()).expire("pwreset:fail:han@example.com", Duration.ofMinutes(10));

    }

    @Test
    @DisplayName("성공하면 틀린 횟수 기록을 지운다.")
    void resetDeletesKey() {

        store.resetFailures("han@example.com");

        verify(redisTemplate).delete("pwreset:fail:han@example.com");

    }

}
