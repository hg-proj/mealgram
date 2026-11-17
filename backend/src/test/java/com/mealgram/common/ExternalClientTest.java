package com.mealgram.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.common.exception.ExternalApiException;
import com.mealgram.common.llm.LlmClient;
import com.sun.net.httpserver.HttpServer;

class ExternalClientTest {

    private static final String API_KEY = "test-key-value";

    private HttpServer server;
    private int status;
    private String body;
    private String lastRequestBody;

    @BeforeEach
    void setUp() throws IOException {

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

    }

    @AfterEach
    void tearDown() {

        server.stop(0);

    }

    private String url() {

        return "http://localhost:" + server.getAddress().getPort() + "/";

    }

    private LlmClient llm() {

        return new LlmClient(API_KEY, "test-model", url());

    }

    @Test
    @DisplayName("LLM 응답에서 내용을 꺼낸다.")
    void llmReturnsContent() {

        status = 200;
        body = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"meals\\\":[]}\"}}]}";

        assertEquals("{\"meals\":[]}", llm().chat("시스템", "사용자"));

    }

    @Test
    @DisplayName("LLM 요청에 응답 길이 상한과 JSON 응답 형식을 담아 보낸다.")
    void llmRequestHasTokenLimit() {

        status = 200;
        body = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"{}\"}}]}";

        llm().chat("시스템", "사용자");

        assertTrue(lastRequestBody.contains("\"max_tokens\":2000"), lastRequestBody);
        assertTrue(lastRequestBody.contains("\"response_format\":{\"type\":\"json_object\"}"), lastRequestBody);

    }

    @Test
    @DisplayName("LLM이 오류 상태를 주면 외부 호출 실패로 알리고 응답 본문과 키는 메시지에 넣지 않는다.")
    void llmErrorStatusBecomesExternalApiException() {

        status = 401;
        body = "{\"error\":{\"message\":\"Incorrect API key provided: sk-secret-value\"}}";

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> llm().chat("시스템", "사용자"));

        assertTrue(e.getMessage().contains("401"));
        assertFalse(e.getMessage().contains("sk-secret-value"));
        assertFalse(e.getMessage().contains(API_KEY));
        assertEquals("LLM", e.getService());

    }

    @Test
    @DisplayName("LLM 응답이 비어 있으면 외부 호출 실패로 알린다.")
    void llmEmptyResponse() {

        status = 200;
        body = "{\"choices\":[]}";

        assertThrows(ExternalApiException.class, () -> llm().chat("시스템", "사용자"));

        body = "{}";

        assertThrows(ExternalApiException.class, () -> llm().chat("시스템", "사용자"));

    }

    @Test
    @DisplayName("LLM 서버에 연결하지 못하면 외부 호출 실패로 알린다.")
    void llmConnectionRefused() {

        LlmClient client = llm();
        server.stop(0);

        assertThrows(ExternalApiException.class, () -> client.chat("시스템", "사용자"));

    }

    @Test
    @DisplayName("키가 없으면 호출하지 않고 설정 오류로 알린다.")
    void blankKeyIsConfigurationError() {

        assertThrows(IllegalStateException.class, () -> new LlmClient("", "test-model", url()).chat("시스템", "사용자"));

    }

}
