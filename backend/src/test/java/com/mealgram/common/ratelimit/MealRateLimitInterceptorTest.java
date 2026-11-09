package com.mealgram.common.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;

class MealRateLimitInterceptorTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private MealRateLimitInterceptor interceptor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.getExpire(anyString())).thenReturn(50L);
        interceptor = new MealRateLimitInterceptor(redisTemplate, 5, 3, 60);
        login(1L);

    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();

    }

    private void login(Long memberId) {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(memberId, null, List.of()));

    }

    private boolean call(String method, MockHttpServletResponse response) {

        return callFrom("127.0.0.1", method, response);

    }

    private boolean callFrom(String address, String method, MockHttpServletResponse response) {

        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/meals");
        request.setRemoteAddr(address);

        return interceptor.preHandle(request, response, new Object());

    }

    @Test
    @DisplayName("제한 횟수까지는 통과시킨다.")
    void allowsUpToLimit() {

        when(valueOperations.increment("ratelimit:meals:1")).thenReturn(1L, 2L, 3L, 4L, 5L);

        for (int i = 0; i < 5; i++) {
            assertTrue(call("POST", new MockHttpServletResponse()));
        }

    }

    @Test
    @DisplayName("제한을 넘으면 429로 막고 다시 시도할 시간을 알려 준다.")
    void blocksOverLimit() {

        when(valueOperations.increment("ratelimit:meals:1")).thenReturn(6L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        BusinessException e = assertThrows(BusinessException.class, () -> call("POST", response));

        assertEquals(ErrorCode.RATE_LIMIT_EXCEEDED, e.getErrorCode());
        assertEquals("50", response.getHeader("Retry-After"));

    }

    @Test
    @DisplayName("처음 호출할 때 만료 시간을 건다.")
    void setsExpireOnFirstCall() {

        when(valueOperations.increment("ratelimit:meals:1")).thenReturn(1L);

        call("POST", new MockHttpServletResponse());

        verify(redisTemplate).expire("ratelimit:meals:1", Duration.ofSeconds(60));

    }

    @Test
    @DisplayName("만료 시간이 빠진 카운터는 다시 만료 시간을 건다.")
    void healsCounterWithoutExpire() {

        when(valueOperations.increment("ratelimit:meals:1")).thenReturn(3L);
        when(redisTemplate.getExpire("ratelimit:meals:1")).thenReturn(-1L);

        call("POST", new MockHttpServletResponse());

        verify(redisTemplate).expire("ratelimit:meals:1", Duration.ofSeconds(60));

    }

    @Test
    @DisplayName("회원마다 따로 센다.")
    void countsPerMember() {

        when(valueOperations.increment("ratelimit:meals:1")).thenReturn(6L);
        when(valueOperations.increment("ratelimit:meals:2")).thenReturn(1L);

        assertThrows(BusinessException.class, () -> call("POST", new MockHttpServletResponse()));

        login(2L);
        assertTrue(call("POST", new MockHttpServletResponse()));

    }

    @Test
    @DisplayName("추천 요청(POST)이 아니면 세지 않는다.")
    void ignoresOtherRequests() {

        assertTrue(call("GET", new MockHttpServletResponse()));

        SecurityContextHolder.clearContext();
        assertTrue(call("GET", new MockHttpServletResponse()));

        verify(valueOperations, never()).increment(anyString());

    }

    @Test
    @DisplayName("로그인하지 않은 요청은 접속 주소별로 세고 회원보다 적게 허용한다.")
    void limitsGuestsByAddress() {

        SecurityContextHolder.clearContext();
        when(valueOperations.increment("ratelimit:meals:guest:10.0.0.1")).thenReturn(3L, 4L);
        when(valueOperations.increment("ratelimit:meals:guest:10.0.0.2")).thenReturn(1L);

        assertTrue(callFrom("10.0.0.1", "POST", new MockHttpServletResponse()));
        BusinessException e = assertThrows(BusinessException.class,
                () -> callFrom("10.0.0.1", "POST", new MockHttpServletResponse()));
        assertEquals(ErrorCode.RATE_LIMIT_EXCEEDED, e.getErrorCode());

        assertTrue(callFrom("10.0.0.2", "POST", new MockHttpServletResponse()));

    }

}
