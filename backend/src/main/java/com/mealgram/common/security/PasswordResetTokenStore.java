package com.mealgram.common.security;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// 비밀번호 재설정 토큰 보관

@Component
public class PasswordResetTokenStore {

    private static final String TOKEN_KEY_PREFIX = "pwreset:token:";
    private static final String MEMBER_KEY_PREFIX = "pwreset:member:";
    private static final String COOLDOWN_KEY_PREFIX = "pwreset:cooldown:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    public PasswordResetTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Long memberId, String token) {

        String memberKey = MEMBER_KEY_PREFIX + memberId;
        String previous = redisTemplate.opsForValue().get(memberKey);
        if (previous != null) {
            redisTemplate.delete(TOKEN_KEY_PREFIX + previous);
        }

        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, String.valueOf(memberId), TOKEN_TTL);
        redisTemplate.opsForValue().set(memberKey, token, TOKEN_TTL);

    }

    public Optional<Long> consume(String token) {

        String memberId = redisTemplate.opsForValue().getAndDelete(TOKEN_KEY_PREFIX + token);
        if (memberId == null) {
            return Optional.empty();
        }

        redisTemplate.delete(MEMBER_KEY_PREFIX + memberId);

        return Optional.of(Long.valueOf(memberId));

    }

    public boolean acquireCooldown(Long memberId) {

        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(COOLDOWN_KEY_PREFIX + memberId, "1", COOLDOWN));

    }

}
