package com.mealgram.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// 비밀번호 재설정 인증번호 보관, 틀린 횟수는 이메일별로 일정 시간 동안 센다

@Component
public class PasswordResetCodeStore {

    private static final String CODE_KEY_PREFIX = "pwreset:code:";
    private static final String FAIL_KEY_PREFIX = "pwreset:fail:";
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration FAIL_WINDOW = Duration.ofMinutes(10);
    private static final int MAX_FAILURES = 5;

    private final StringRedisTemplate redisTemplate;

    public PasswordResetCodeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Long memberId, String code) {

        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + memberId, code, CODE_TTL);

    }

    public boolean consume(Long memberId, String code) {

        String saved = redisTemplate.opsForValue().get(CODE_KEY_PREFIX + memberId);
        if (saved == null || !MessageDigest.isEqual(saved.getBytes(StandardCharsets.UTF_8), code.getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        redisTemplate.delete(CODE_KEY_PREFIX + memberId);

        return true;

    }

    public boolean isBlocked(String email) {

        String count = redisTemplate.opsForValue().get(FAIL_KEY_PREFIX + email);

        return count != null && Long.parseLong(count) >= MAX_FAILURES;

    }

    public void recordFailure(String email) {

        String key = FAIL_KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && (count == 1 || isWithoutExpiry(key))) {
            redisTemplate.expire(key, FAIL_WINDOW);
        }

    }

    public void resetFailures(String email) {

        redisTemplate.delete(FAIL_KEY_PREFIX + email);

    }

    private boolean isWithoutExpiry(String key) {

        Long ttl = redisTemplate.getExpire(key);

        return ttl == null || ttl < 0;

    }

}
