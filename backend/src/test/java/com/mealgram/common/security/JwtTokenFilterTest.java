package com.mealgram.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class JwtTokenFilterTest {

    private JwtTokenProvider provider;
    private JwtTokenFilter filter;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();
        provider = new JwtTokenProvider(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));
        filter = new JwtTokenFilter(provider);

    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();

    }

    private void request(String token) throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    }

    @Test
    @DisplayName("accessToken이면 인증한다.")
    void authenticatesAccessToken() throws Exception {

        request(provider.generateAccessToken(3L));

        assertEquals(3L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

    }

    @Test
    @DisplayName("refreshToken으로는 인증하지 않는다.")
    void doesNotAuthenticateRefreshToken() throws Exception {

        request(provider.generateRefreshToken(3L));

        assertNull(SecurityContextHolder.getContext().getAuthentication());

    }

    @Test
    @DisplayName("토큰이 없거나 틀리면 인증하지 않는다.")
    void doesNotAuthenticateMissingOrBrokenToken() throws Exception {

        request(null);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        request("broken.token.value");
        assertNull(SecurityContextHolder.getContext().getAuthentication());

    }

}
