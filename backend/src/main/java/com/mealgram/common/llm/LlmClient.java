package com.mealgram.common.llm;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

// 프롬프트를 보내 JSON 응답을 받는 LLM 호출

@Component
public class LlmClient {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SYSTEM_ROLE = "system";
    private static final String USER_ROLE = "user";
    private static final String JSON_FORMAT = "json_object";
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(120);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public LlmClient(@Value("${openai.api-key}") String apiKey,
                     @Value("${openai.chat-model}") String model,
                     @Value("${openai.chat-url}") String url) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder().baseUrl(url).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String chat(String systemPrompt, String userPrompt) {

        if (apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 필요합니다.");
        }

        ChatRequest request = new ChatRequest(model,
                List.of(new Message(SYSTEM_ROLE, systemPrompt), new Message(USER_ROLE, userPrompt)),
                new ResponseFormat(JSON_FORMAT));

        ChatResponse response = restClient.post()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + apiKey)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        return response.choices().get(0).message().content();

    }

    private record ChatRequest(String model,
                               List<Message> messages,
                               @JsonProperty("response_format") ResponseFormat responseFormat) {

    }

    private record Message(String role, String content) {

    }

    private record ResponseFormat(String type) {

    }

    private record ChatResponse(List<Choice> choices) {

    }

    private record Choice(Message message) {

    }

}
