package com.mealgram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ProfilePropertiesTest {

    private Properties load(String name) throws IOException {

        Properties properties = new Properties();
        try (InputStream in = new ClassPathResource(name).getInputStream()) {
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }

        return properties;

    }

    @Test
    @DisplayName("프로필을 정하지 않으면 개발 프로필로 실행한다.")
    void defaultProfileIsDev() throws IOException {

        assertEquals("dev", load("application.properties").getProperty("spring.profiles.default"));

    }

    @Test
    @DisplayName("운영 프로필의 비밀값은 기본값 없이 환경변수로만 받는다.")
    void prodSecretsComeOnlyFromEnvironment() throws IOException {

        Properties prod = load("application-prod.properties");

        assertEquals("${JWT_SECRET}", prod.getProperty("jwt.secret"));
        assertEquals("${DB_PASSWORD}", prod.getProperty("spring.datasource.password"));
        assertEquals("${DB_URL}", prod.getProperty("spring.datasource.url"));
        assertEquals("${DB_USERNAME}", prod.getProperty("spring.datasource.username"));
        assertEquals("${OPENAI_API_KEY}", prod.getProperty("openai.api-key"));

    }

    @Test
    @DisplayName("운영 프로필은 오류 내용을 숨기고 종료 때 진행 중인 요청을 마무리한다.")
    void prodHidesErrorDetailsAndShutsDownGracefully() throws IOException {

        Properties prod = load("application-prod.properties");

        assertEquals("never", prod.getProperty("server.error.include-message"));
        assertEquals("never", prod.getProperty("server.error.include-stacktrace"));
        assertEquals("graceful", prod.getProperty("server.shutdown"));
        assertEquals("INFO", prod.getProperty("logging.level.com.mealgram"));

    }

    @Test
    @DisplayName("개발 프로필은 우리 코드의 로그를 자세히 남긴다.")
    void devLogsDebug() throws IOException {

        assertEquals("DEBUG", load("application-dev.properties").getProperty("logging.level.com.mealgram"));
        assertTrue(load("application-dev.properties").stringPropertyNames().stream().noneMatch(key -> key.startsWith("jwt")));

    }

}
