package com.mealgram.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class RefreshTokenStoreTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RefreshTokenStore store;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        JwtTokenProvider provider = new JwtTokenProvider(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));
        store = new RefreshTokenStore(redisTemplate, provider);

    }

    @Test
    @DisplayName("refreshToken을 14일 동안 보관한다.")
    void savesWithFourteenDays() {

        store.save(1L, "token");

        verify(valueOperations).set("refresh:1", "token", Duration.ofDays(14));

    }

    @Test
    @DisplayName("보관한 refreshToken을 찾고 없으면 비어 있다.")
    void findsStoredToken() {

        when(valueOperations.get("refresh:1")).thenReturn("token");
        when(valueOperations.get("refresh:2")).thenReturn(null);

        assertEquals(Optional.of("token"), store.find(1L));
        assertTrue(store.find(2L).isEmpty());

    }

    @Test
    @DisplayName("보관한 refreshToken을 지운다.")
    void deletesToken() {

        store.delete(1L);

        verify(redisTemplate).delete("refresh:1");

    }

}
