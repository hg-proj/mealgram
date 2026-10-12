package com.mealgram.common.embedding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// 질문 임베딩 Redis 캐시

@Slf4j
@Component
public class EmbeddingCache {

    private static final String KEY_PREFIX = "embedding:";
    private static final String SEPARATOR = ",";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final String model;

    public EmbeddingCache(StringRedisTemplate redisTemplate, @Value("${openai.embedding-model}") String model) {
        this.redisTemplate = redisTemplate;
        this.model = model;
    }

    public Optional<float[]> find(String text) {

        try {
            String value = redisTemplate.opsForValue().get(keyOf(text));
            return value == null ? Optional.empty() : Optional.of(parse(value));
        } catch (DataAccessException | IllegalArgumentException e) {
            log.warn("임베딩 캐시를 읽지 못했습니다: {}", e.getClass().getSimpleName());
            return Optional.empty();
        }

    }

    public void save(String text, float[] vector) {

        try {
            redisTemplate.opsForValue().set(keyOf(text), format(vector), TTL);
        } catch (DataAccessException e) {
            log.warn("임베딩 캐시를 저장하지 못했습니다: {}", e.getClass().getSimpleName());
        }

    }

    String keyOf(String text) {

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + model + ":" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

    }

    private String format(float[] vector) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(SEPARATOR);
            }
            builder.append(vector[i]);
        }

        return builder.toString();

    }

    private float[] parse(String value) {

        String[] parts = value.split(SEPARATOR);
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i]);
        }

        return vector;

    }

}
