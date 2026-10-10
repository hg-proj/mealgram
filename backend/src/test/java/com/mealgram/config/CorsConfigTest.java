package com.mealgram.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

    private CorsConfiguration configurationFor(String frontendUrls) {

        CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(frontendUrls);

        return source.getCorsConfiguration(new MockHttpServletRequest("GET", "/meals"));

    }

    @Test
    @DisplayName("프론트 주소를 허용 출처로 등록한다.")
    void allowsFrontendOrigin() {

        CorsConfiguration configuration = configurationFor("http://localhost:5173");

        assertNotNull(configuration);
        assertEquals("http://localhost:5173", configuration.checkOrigin("http://localhost:5173"));
        assertNull(configuration.checkOrigin("https://evil.example.com"));

    }

    @Test
    @DisplayName("쉼표로 나눈 여러 주소와 끝의 슬래시를 처리한다.")
    void parsesMultipleOrigins() {

        CorsConfiguration configuration = configurationFor("http://localhost:5173, https://mealgram.example.com/");

        assertEquals("http://localhost:5173", configuration.checkOrigin("http://localhost:5173"));
        assertEquals("https://mealgram.example.com", configuration.checkOrigin("https://mealgram.example.com"));

    }

    @Test
    @DisplayName("인증 헤더와 본문 형식을 허용하고 Retry-After를 읽을 수 있게 노출한다.")
    void allowsHeadersAndExposesRetryAfter() {

        CorsConfiguration configuration = configurationFor("http://localhost:5173");

        assertTrue(configuration.getAllowedHeaders().contains("Authorization"));
        assertTrue(configuration.getAllowedHeaders().contains("Content-Type"));
        assertTrue(configuration.getExposedHeaders().contains("Retry-After"));
        assertTrue(configuration.getAllowedMethods().contains("PATCH"));

    }

}
