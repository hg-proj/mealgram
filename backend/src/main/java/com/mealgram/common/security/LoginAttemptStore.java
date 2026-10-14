package com.mealgram.common.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// 로그인 실패 횟수 보관(아이디별, 일정 시간 동안)

@Component
public class LoginAttemptStore {

    private static final String KEY_PREFIX = "login:fail:";
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;

    public LoginAttemptStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isBlocked(String loginId) {

        String count = redisTemplate.opsForValue().get(KEY_PREFIX + loginId);

        return count != null && Long.parseLong(count) >= MAX_FAILURES;

    }

    public void recordFailure(String loginId) {

        String key = KEY_PREFIX + loginId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && (count == 1 || isWithoutExpiry(key))) {
            redisTemplate.expire(key, WINDOW);
        }

    }

    public void reset(String loginId) {

        redisTemplate.delete(KEY_PREFIX + loginId);

    }

    private boolean isWithoutExpiry(String key) {

        Long ttl = redisTemplate.getExpire(key);

        return ttl == null || ttl < 0;

    }

}
