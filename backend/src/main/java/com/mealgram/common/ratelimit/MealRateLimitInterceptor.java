package com.mealgram.common.ratelimit;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 식단 추천 호출 횟수 제한

@Component
public class MealRateLimitInterceptor implements HandlerInterceptor {

    private static final String KEY_PREFIX = "ratelimit:meals:";
    private static final String RETRY_AFTER = "Retry-After";

    private final StringRedisTemplate redisTemplate;
    private final int maxRequests;
    private final Duration window;

    public MealRateLimitInterceptor(StringRedisTemplate redisTemplate,
                                    @Value("${rate-limit.meals.max-requests}") int maxRequests,
                                    @Value("${rate-limit.meals.window-seconds}") long windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxRequests = maxRequests;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Long memberId = currentMemberId();
        if (!"POST".equals(request.getMethod()) || memberId == null) {
            return true;
        }

        String key = KEY_PREFIX + memberId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return true;
        }
        if (count == 1 || redisTemplate.getExpire(key) < 0) {
            redisTemplate.expire(key, window);
        }

        if (count > maxRequests) {
            Long remaining = redisTemplate.getExpire(key);
            response.setHeader(RETRY_AFTER, String.valueOf(remaining == null || remaining < 1 ? 1 : remaining));
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        return true;

    }

    private Long currentMemberId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long memberId) {
            return memberId;
        }

        return null;

    }

}
