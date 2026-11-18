package com.mealgram.common.security;

import java.time.Duration;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// 로그인 실패 횟수 보관(아이디별, 일정 시간 동안)

@Slf4j
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

        try {
            String count = redisTemplate.opsForValue().get(KEY_PREFIX + loginId);

            return count != null && Long.parseLong(count) >= MAX_FAILURES;
        } catch (DataAccessException e) {
            log.warn("로그인 실패 기록을 읽지 못해 막지 않습니다: {}", e.getClass().getSimpleName());
            return false;
        }

    }

    public void recordFailure(String loginId) {

        String key = KEY_PREFIX + loginId;

        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && (count == 1 || isWithoutExpiry(key))) {
                redisTemplate.expire(key, WINDOW);
            }
        } catch (DataAccessException e) {
            log.warn("로그인 실패를 기록하지 못했습니다: {}", e.getClass().getSimpleName());
        }

    }

    public void reset(String loginId) {

        try {
            redisTemplate.delete(KEY_PREFIX + loginId);
        } catch (DataAccessException e) {
            log.warn("로그인 실패 기록을 지우지 못했습니다: {}", e.getClass().getSimpleName());
        }

    }

    private boolean isWithoutExpiry(String key) {

        Long ttl = redisTemplate.getExpire(key);

        return ttl == null || ttl < 0;

    }

}
