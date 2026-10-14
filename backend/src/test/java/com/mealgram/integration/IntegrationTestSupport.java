package com.mealgram.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.mealgram.common.embedding.EmbeddingClient;
import com.mealgram.common.llm.LlmClient;
import com.mealgram.common.mail.PasswordResetMailer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 통합 테스트 공통 설정

@SpringBootTest(properties = {
        "recipe.import.enabled=true",
        "recipe.embedding.enabled=false",
        "rate-limit.meals.max-requests=3",
        "rate-limit.meals.window-seconds=60",
        "app.frontend-url=http://localhost:5173"
})
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("mealgram").withUsername("postgres").withPassword("test");
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);
    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final String PASSWORD = "Valid-pass12!";

    static {
        if (dockerAvailable()) {
            POSTGRES.start();
            REDIS.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                REDIS.stop();
                POSTGRES.stop();
            }));
        }
    }

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected EmbeddingClient embeddingClient;

    @MockitoBean
    protected LlmClient llmClient;

    @MockitoBean
    protected PasswordResetMailer passwordResetMailer;

    public static boolean dockerAvailable() {

        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException e) {
            return false;
        }

    }

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));

    }

    protected String unique(String prefix) {

        return prefix + SEQUENCE.incrementAndGet() + (System.nanoTime() % 100000);

    }

    protected String password() {

        return PASSWORD;

    }

    protected String json(Object body) {

        return MAPPER.writeValueAsString(body);

    }

    protected JsonNode body(MvcResult result) throws Exception {

        String content = result.getResponse().getContentAsString();

        return content.isEmpty() ? MAPPER.createObjectNode() : MAPPER.readTree(content);

    }

    protected MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder builder, String token) {

        return token == null ? builder : builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    }

    protected MvcResult postJson(String path, Object body, String token) throws Exception {

        return mockMvc.perform(withToken(post(path), token).contentType(MediaType.APPLICATION_JSON).content(json(body))).andReturn();

    }

    protected MvcResult patchJson(String path, Object body, String token) throws Exception {

        return mockMvc.perform(withToken(patch(path), token).contentType(MediaType.APPLICATION_JSON).content(json(body))).andReturn();

    }

    protected MvcResult getJson(String path, String token) throws Exception {

        return mockMvc.perform(withToken(get(path), token)).andReturn();

    }

    protected MvcResult deleteJson(String path, String token) throws Exception {

        return mockMvc.perform(withToken(delete(path), token)).andReturn();

    }

    protected Member signupAndLogin(String prefix) throws Exception {

        String loginId = unique(prefix);
        String email = loginId + "@example.com";
        postJson("/auth/signup", Map.of("nickname", "테스터", "loginId", loginId, "password", PASSWORD,
                "email", email, "age", 30, "gender", "FEMALE", "height", 160, "weight", 55, "activityLevel", "LIGHT"), null);
        JsonNode login = body(postJson("/auth/login", Map.of("loginId", loginId, "password", PASSWORD), null));

        return new Member(loginId, email, login.get("accessToken").asString(), login.get("refreshToken").asString());

    }

    protected record Member(String loginId, String email, String accessToken, String refreshToken) {
    }

}
