package com.mealgram.common.security;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// refreshToken 보관

@Component
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenStore(StringRedisTemplate redisTemplate, JwtTokenProvider jwtTokenProvider) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void save(Long memberId, String refreshToken) {

        redisTemplate.opsForValue().set(KEY_PREFIX + memberId, refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()));

    }

    public Optional<String> find(Long memberId) {

        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + memberId));

    }

    public void delete(Long memberId) {

        redisTemplate.delete(KEY_PREFIX + memberId);

    }

}
