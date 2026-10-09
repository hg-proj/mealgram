package com.mealgram.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {

        provider = new JwtTokenProvider(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));

    }

    @Test
    @DisplayName("accessToken은 accessToken으로만 인정한다.")
    void acceptsAccessTokenOnlyAsAccess() {

        String token = provider.generateAccessToken(1L);

        assertTrue(provider.isAccessToken(token));
        assertFalse(provider.isRefreshToken(token));

    }

    @Test
    @DisplayName("refreshToken은 refreshToken으로만 인정한다.")
    void acceptsRefreshTokenOnlyAsRefresh() {

        String token = provider.generateRefreshToken(1L);

        assertTrue(provider.isRefreshToken(token));
        assertFalse(provider.isAccessToken(token));

    }

    @Test
    @DisplayName("토큰에서 회원 번호를 꺼낸다.")
    void readsMemberId() {

        assertEquals(7L, provider.getMemberId(provider.generateAccessToken(7L)));
        assertEquals(7L, provider.getMemberId(provider.generateRefreshToken(7L)));

    }

    @Test
    @DisplayName("형식이 틀리거나 다른 키로 만든 토큰은 인정하지 않는다.")
    void rejectsInvalidTokens() {

        JwtTokenProvider other = new JwtTokenProvider(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));

        assertFalse(provider.isAccessToken("abc.def.ghi"));
        assertFalse(provider.isRefreshToken(""));
        assertFalse(provider.isAccessToken(other.generateAccessToken(1L)));

    }

    @Test
    @DisplayName("종류 표시가 없는 예전 토큰은 인정하지 않는다.")
    void rejectsTokenWithoutType() {

        String legacy = Jwts.builder().subject("1").signWith(
                io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(
                        Encoders.BASE64.encode(new byte[32])))).compact();

        assertFalse(provider.isAccessToken(legacy));
        assertFalse(provider.isRefreshToken(legacy));

    }

}
