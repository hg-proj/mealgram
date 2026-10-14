package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.mail.PasswordResetMailer;
import com.mealgram.common.security.JwtTokenProvider;
import com.mealgram.common.security.LoginAttemptStore;
import com.mealgram.common.security.PasswordResetTokenStore;
import com.mealgram.common.security.RefreshTokenStore;
import com.mealgram.member.dto.LoginRequest;
import com.mealgram.member.dto.LoginResponse;

class LoginProtectionTest {

    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;
    private LoginAttemptStore loginAttemptStore;
    private AuthService authService;

    @BeforeEach
    void setUp() {

        memberRepository = mock(MemberRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        loginAttemptStore = mock(LoginAttemptStore.class);
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        when(provider.generateAccessToken(1L)).thenReturn("access");
        when(provider.generateRefreshToken(1L)).thenReturn("refresh");
        authService = new AuthService(memberRepository, passwordEncoder, provider, mock(RefreshTokenStore.class),
                mock(PasswordResetTokenStore.class), mock(PasswordResetMailer.class), loginAttemptStore);

    }

    private ErrorCode errorOf(String loginId, String password) {

        return assertThrows(BusinessException.class, () -> authService.login(new LoginRequest(loginId, password)))
                .getErrorCode();

    }

    @Test
    @DisplayName("실패가 쌓여 막힌 아이디는 비밀번호를 확인하지 않고 429 오류로 알린다.")
    void blockedLoginIsRejectedWithoutPasswordCheck() {

        when(loginAttemptStore.isBlocked("hangyeol01")).thenReturn(true);

        assertEquals(ErrorCode.LOGIN_BLOCKED, errorOf("hangyeol01", "password12!"));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(memberRepository, never()).findByLoginId(anyString());

    }

    @Test
    @DisplayName("비밀번호가 틀리면 실패를 기록한다.")
    void wrongPasswordRecordsFailure() {

        Member member = Member.builder().id(1L).loginId("hangyeol01").password("encoded").build();
        when(memberRepository.findByLoginId("hangyeol01")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertEquals(ErrorCode.INVALID_CREDENTIALS, errorOf("hangyeol01", "wrong"));
        verify(loginAttemptStore).recordFailure("hangyeol01");
        verify(loginAttemptStore, never()).reset(anyString());

    }

    @Test
    @DisplayName("없는 아이디도 같은 오류로 알리고 실패를 기록한다.")
    void unknownLoginIdRecordsFailure() {

        when(memberRepository.findByLoginId("nobody01")).thenReturn(Optional.empty());

        assertEquals(ErrorCode.INVALID_CREDENTIALS, errorOf("nobody01", "password12!"));
        verify(loginAttemptStore).recordFailure("nobody01");

    }

    @Test
    @DisplayName("로그인에 성공하면 실패 기록을 지운다.")
    void successResetsFailures() {

        Member member = Member.builder().id(1L).loginId("hangyeol01").password("encoded").build();
        when(memberRepository.findByLoginId("hangyeol01")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password12!", "encoded")).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest("hangyeol01", "password12!"));

        assertEquals("access", response.accessToken());
        assertTrue(response.refreshToken().equals("refresh"));
        verify(loginAttemptStore).reset("hangyeol01");
        verify(loginAttemptStore, never()).recordFailure(anyString());

    }

}
