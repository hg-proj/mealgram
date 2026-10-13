package com.mealgram.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mealgram.common.security.JwtAuthenticationEntryPoint;
import com.mealgram.common.security.JwtTokenFilter;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig(mock(JwtTokenFilter.class), mock(JwtAuthenticationEntryPoint.class));

    @Test
    @DisplayName("기본 사용자 서비스는 어떤 이름으로도 사용자를 찾지 않는다.")
    void userDetailsServiceFindsNobody() {

        UserDetailsService service = config.userDetailsService();

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("user"));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("admin"));

    }

}
