package com.mealgram.common.embedding;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.mealgram.common.exception.ExternalApiException;

import lombok.extern.slf4j.Slf4j;

// 텍스트를 임베딩 벡터로 변환하는 OpenAI 호출

@Slf4j
@Component
public class EmbeddingClient {

    private static final String SERVICE = "임베딩";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public EmbeddingClient(@Value("${openai.api-key}") String apiKey,
                           @Value("${openai.embedding-model}") String model,
                           @Value("${openai.embedding-url}") String url) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public List<float[]> embed(List<String> texts) {

        if (apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 필요합니다.");
        }

        long start = System.currentTimeMillis();

        try {
            EmbeddingResponse response = restClient.post()
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + apiKey)
                    .body(new EmbeddingRequest(model, texts))
                    .retrieve()
                    .body(EmbeddingResponse.class);

            return vectorsOf(response, texts.size());

        } catch (RestClientResponseException e) {
            log.warn("{} 호출 실패: 상태 {}, {}ms", SERVICE, e.getStatusCode().value(), elapsed(start));
            throw new ExternalApiException(SERVICE, "상태 " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            log.warn("{} 호출 실패: {}, {}ms", SERVICE, e.getClass().getSimpleName(), elapsed(start));
            throw new ExternalApiException(SERVICE, e.getClass().getSimpleName(), e);
        }

    }

    private List<float[]> vectorsOf(EmbeddingResponse response, int expectedSize) {

        if (response == null || response.data() == null) {
            throw new ExternalApiException(SERVICE, "응답이 비어 있습니다.");
        }

        List<float[]> vectors = response.data().stream()
                .sorted(Comparator.comparingInt(EmbeddingItem::index))
                .map(EmbeddingItem::embedding)
                .toList();

        if (vectors.size() != expectedSize) {
            throw new ExternalApiException(SERVICE, "응답 개수가 요청과 다릅니다.");
        }

        return vectors;

    }

    private long elapsed(long start) {

        return System.currentTimeMillis() - start;

    }

    private record EmbeddingRequest(String model, List<String> input) {
    }

    private record EmbeddingResponse(List<EmbeddingItem> data) {
    }

    private record EmbeddingItem(int index, float[] embedding) {
    }

}
