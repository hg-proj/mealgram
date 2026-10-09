package com.mealgram.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class PasswordResetTokenStoreTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private PasswordResetTokenStore store;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new PasswordResetTokenStore(redisTemplate);

    }

    @Test
    @DisplayName("토큰을 30분 동안 보관한다.")
    void savesForThirtyMinutes() {

        store.save(1L, "new");

        verify(valueOperations).set("pwreset:token:new", "1", Duration.ofMinutes(30));
        verify(valueOperations).set("pwreset:member:1", "new", Duration.ofMinutes(30));
        verify(redisTemplate, never()).delete("pwreset:token:null");

    }

    @Test
    @DisplayName("새 토큰을 만들면 이전 토큰은 지운다.")
    void removesPreviousToken() {

        when(valueOperations.get("pwreset:member:1")).thenReturn("old");

        store.save(1L, "new");

        verify(redisTemplate).delete("pwreset:token:old");

    }

    @Test
    @DisplayName("토큰을 쓰면 회원 번호를 주고 토큰을 지운다.")
    void consumesToken() {

        when(valueOperations.getAndDelete("pwreset:token:abc")).thenReturn("1");

        assertEquals(Optional.of(1L), store.consume("abc"));
        verify(redisTemplate).delete("pwreset:member:1");

    }

    @Test
    @DisplayName("없거나 이미 쓴 토큰이면 비어 있다.")
    void consumesUnknownToken() {

        when(valueOperations.getAndDelete("pwreset:token:abc")).thenReturn(null);

        assertTrue(store.consume("abc").isEmpty());
        verify(redisTemplate, never()).delete("pwreset:member:null");

    }

    @Test
    @DisplayName("60초 안에 다시 요청하면 쿨다운으로 막는다.")
    void acquiresCooldown() {

        when(valueOperations.setIfAbsent("pwreset:cooldown:1", "1", Duration.ofSeconds(60))).thenReturn(true, false);

        assertTrue(store.acquireCooldown(1L));
        assertFalse(store.acquireCooldown(1L));

    }

}
