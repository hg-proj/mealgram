package com.mealgram.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// 회원가입 이메일 인증 보관

@Component
public class SignupVerificationStore {

    private static final String CODE_KEY_PREFIX = "signup:code:";
    private static final String VERIFIED_KEY_PREFIX = "signup:verified:";
    private static final String FAIL_KEY_PREFIX = "signup:fail:";
    private static final String COOLDOWN_KEY_PREFIX = "signup:cooldown:";
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final Duration FAIL_WINDOW = Duration.ofMinutes(10);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_FAILURES = 5;

    private final StringRedisTemplate redisTemplate;

    public SignupVerificationStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveCode(String email, String code) {

        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + email, code, CODE_TTL);
        redisTemplate.delete(VERIFIED_KEY_PREFIX + email);

    }

    public void deleteCode(String email) {

        redisTemplate.delete(CODE_KEY_PREFIX + email);

    }

    public boolean consumeCode(String email, String code) {

        String saved = redisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);

        if (saved == null
                || !MessageDigest.isEqual(saved.getBytes(StandardCharsets.UTF_8), code.getBytes(StandardCharsets.UTF_8))) {
            return false;
        }

        redisTemplate.delete(CODE_KEY_PREFIX + email);

        return true;

    }

    public void markVerified(String email) {

        redisTemplate.opsForValue().set(VERIFIED_KEY_PREFIX + email, "1", VERIFIED_TTL);

    }

    public boolean isVerified(String email) {

        return Boolean.TRUE.equals(redisTemplate.hasKey(VERIFIED_KEY_PREFIX + email));

    }

    public void clear(String email) {

        redisTemplate.delete(VERIFIED_KEY_PREFIX + email);
        redisTemplate.delete(CODE_KEY_PREFIX + email);

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

    public boolean acquireCooldown(String email) {

        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(COOLDOWN_KEY_PREFIX + email, "1", COOLDOWN));

    }

    public void releaseCooldown(String email) {

        redisTemplate.delete(COOLDOWN_KEY_PREFIX + email);

    }

    private boolean isWithoutExpiry(String key) {

        Long ttl = redisTemplate.getExpire(key);

        return ttl == null || ttl < 0;

    }

}
