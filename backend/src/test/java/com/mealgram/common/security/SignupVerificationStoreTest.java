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

class SignupVerificationStoreTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private SignupVerificationStore store;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new SignupVerificationStore(redisTemplate);

    }

    @Test
    @DisplayName("인증번호는 10분 동안 보관하고 이전 인증 완료 기록은 지운다.")
    void savesCodeForTenMinutes() {

        store.saveCode("a@example.com", "123456");

        verify(valueOperations).set("signup:code:a@example.com", "123456", Duration.ofMinutes(10));
        verify(redisTemplate).delete("signup:verified:a@example.com");

    }

    @Test
    @DisplayName("인증번호가 맞으면 지우고 true를 돌려준다.")
    void consumesMatchingCode() {

        when(valueOperations.get("signup:code:a@example.com")).thenReturn("123456");

        assertTrue(store.consumeCode("a@example.com", "123456"));
        verify(redisTemplate).delete("signup:code:a@example.com");

    }

    @Test
    @DisplayName("인증번호가 없거나 틀리면 false이고 지우지 않는다.")
    void rejectsMissingOrWrongCode() {

        when(valueOperations.get("signup:code:a@example.com")).thenReturn("123456");

        assertFalse(store.consumeCode("a@example.com", "000000"));
        assertFalse(store.consumeCode("none@example.com", "123456"));
        verify(redisTemplate, never()).delete("signup:code:a@example.com");

    }

    @Test
    @DisplayName("인증 완료는 30분 동안 유지하고 가입을 마치면 지운다.")
    void keepsVerifiedForThirtyMinutes() {

        store.markVerified("a@example.com");
        when(redisTemplate.hasKey("signup:verified:a@example.com")).thenReturn(true);

        verify(valueOperations).set("signup:verified:a@example.com", "1", Duration.ofMinutes(30));
        assertTrue(store.isVerified("a@example.com"));

        store.clear("a@example.com");

        verify(redisTemplate).delete("signup:verified:a@example.com");

    }

    @Test
    @DisplayName("틀린 횟수가 5회가 되면 막는다.")
    void blocksFromFifthFailure() {

        when(valueOperations.get("signup:fail:a@example.com")).thenReturn("4");
        assertFalse(store.isBlocked("a@example.com"));

        when(valueOperations.get("signup:fail:a@example.com")).thenReturn("5");
        assertTrue(store.isBlocked("a@example.com"));

    }

    @Test
    @DisplayName("처음 틀리면 10분 만료를 설정한다.")
    void firstFailureSetsExpiry() {

        when(valueOperations.increment("signup:fail:a@example.com")).thenReturn(1L);

        store.recordFailure("a@example.com");

        verify(redisTemplate).expire("signup:fail:a@example.com", Duration.ofMinutes(10));

    }

    @Test
    @DisplayName("재요청 대기는 60초이고 이미 있으면 얻지 못한다.")
    void cooldownIsSixtySeconds() {

        when(valueOperations.setIfAbsent("signup:cooldown:a@example.com", "1", Duration.ofSeconds(60))).thenReturn(true, false);

        assertTrue(store.acquireCooldown("a@example.com"));
        assertFalse(store.acquireCooldown("a@example.com"));

    }

}
