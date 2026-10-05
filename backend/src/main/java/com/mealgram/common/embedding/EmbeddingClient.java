package com.mealgram.common.embedding;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// 텍스트를 임베딩 벡터로 변환하는 OpenAI 호출

@Component
public class EmbeddingClient {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public EmbeddingClient(@Value("${openai.api-key}") String apiKey,
                           @Value("${openai.embedding-model}") String model,
                           @Value("${openai.embedding-url}") String url) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder().baseUrl(url).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public List<float[]> embed(List<String> texts) {

        if (apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 필요합니다.");
        }

        EmbeddingResponse response = restClient.post()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + apiKey)
                .body(new EmbeddingRequest(model, texts))
                .retrieve()
                .body(EmbeddingResponse.class);

        List<float[]> vectors = response.data().stream()
                .sorted(Comparator.comparingInt(EmbeddingItem::index))
                .map(EmbeddingItem::embedding)
                .toList();

        if (vectors.size() != texts.size()) {
            throw new IllegalStateException("임베딩 응답 개수가 요청과 다릅니다.");
        }

        return vectors;

    }

    private record EmbeddingRequest(String model, List<String> input) {

    }

    private record EmbeddingResponse(List<EmbeddingItem> data) {

    }

    private record EmbeddingItem(int index, float[] embedding) {

    }

}
