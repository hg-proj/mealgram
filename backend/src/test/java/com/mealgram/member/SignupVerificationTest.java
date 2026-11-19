package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.mail.PasswordResetMailer;
import com.mealgram.common.security.JwtTokenProvider;
import com.mealgram.common.security.LoginAttemptStore;
import com.mealgram.common.security.PasswordResetCodeStore;
import com.mealgram.common.security.PasswordResetTokenStore;
import com.mealgram.common.security.RefreshTokenStore;
import com.mealgram.common.security.SignupVerificationStore;
import com.mealgram.member.dto.SignupCodeRequest;
import com.mealgram.member.dto.SignupCodeVerifyRequest;
import com.mealgram.member.dto.SignupRequest;

class SignupVerificationTest {

    private static final String EMAIL = "han@example.com";

    private MemberRepository memberRepository;
    private PasswordResetMailer mailer;
    private SignupVerificationStore store;
    private AuthService authService;

    @BeforeEach
    void setUp() {

        memberRepository = mock(MemberRepository.class);
        mailer = mock(PasswordResetMailer.class);
        store = mock(SignupVerificationStore.class);
        authService = new AuthService(memberRepository, mock(PasswordEncoder.class), mock(JwtTokenProvider.class),
                mock(RefreshTokenStore.class), mock(PasswordResetTokenStore.class), mock(PasswordResetCodeStore.class),
                mailer, mock(LoginAttemptStore.class), store);

    }

    private SignupRequest signup() {

        return new SignupRequest("한결", "hangyeol01", "password12!", null, null, null, null, null, EMAIL);

    }

    @Test
    @DisplayName("인증번호를 보내면 이메일을 소문자로 바꿔 6자리 숫자를 저장하고 메일로 보낸다.")
    void sendsCode() {

        when(store.acquireCooldown(EMAIL)).thenReturn(true);

        authService.sendSignupCode(new SignupCodeRequest(" Han@Example.COM ", "한결"));

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(store).saveCode(eq(EMAIL), code.capture());
        verify(mailer).sendSignupCode(EMAIL, "한결", code.getValue());
        assertEquals(6, code.getValue().length());

    }

    @Test
    @DisplayName("이미 가입된 이메일에는 인증번호를 보내지 않고 409로 알린다.")
    void rejectsRegisteredEmail() {

        when(memberRepository.existsByEmail(EMAIL)).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.sendSignupCode(new SignupCodeRequest(EMAIL, null)));

        assertEquals(ErrorCode.DUPLICATE_EMAIL, e.getErrorCode());
        verify(mailer, never()).sendSignupCode(anyString(), org.mockito.ArgumentMatchers.any(), anyString());

    }

    @Test
    @DisplayName("재요청 대기 중이면 다시 보내지 않는다.")
    void skipsDuringCooldown() {

        when(store.acquireCooldown(EMAIL)).thenReturn(false);

        authService.sendSignupCode(new SignupCodeRequest(EMAIL, null));

        verify(store, never()).saveCode(anyString(), anyString());
        verify(mailer, never()).sendSignupCode(anyString(), org.mockito.ArgumentMatchers.any(), anyString());

    }

    @Test
    @DisplayName("메일을 보내지 못하면 저장한 인증번호와 재요청 대기를 되돌린다.")
    void rollsBackWhenMailFails() {

        when(store.acquireCooldown(EMAIL)).thenReturn(true);
        doThrow(new BusinessException(ErrorCode.MAIL_SEND_FAILED)).when(mailer).sendSignupCode(eq(EMAIL), org.mockito.ArgumentMatchers.any(), anyString());

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.sendSignupCode(new SignupCodeRequest(EMAIL, null)));

        assertEquals(ErrorCode.MAIL_SEND_FAILED, e.getErrorCode());
        verify(store).deleteCode(EMAIL);
        verify(store).releaseCooldown(EMAIL);

    }

    @Test
    @DisplayName("인증번호가 맞으면 인증 완료로 기록하고 틀린 횟수를 지운다.")
    void verifiesCode() {

        when(store.consumeCode(EMAIL, "123456")).thenReturn(true);

        authService.verifySignupCode(new SignupCodeVerifyRequest(EMAIL, "123456"));

        verify(store).markVerified(EMAIL);
        verify(store).resetFailures(EMAIL);

    }

    @Test
    @DisplayName("인증번호가 틀리면 틀린 횟수를 세고 400으로 알린다.")
    void rejectsWrongCode() {

        when(store.consumeCode(EMAIL, "000000")).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.verifySignupCode(new SignupCodeVerifyRequest(EMAIL, "000000")));

        assertEquals(ErrorCode.INVALID_SIGNUP_CODE, e.getErrorCode());
        verify(store).recordFailure(EMAIL);
        verify(store, never()).markVerified(anyString());

    }

    @Test
    @DisplayName("너무 많이 틀리면 인증번호를 확인하지 않고 429로 막는다.")
    void blocksAfterTooManyFailures() {

        when(store.isBlocked(EMAIL)).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.verifySignupCode(new SignupCodeVerifyRequest(EMAIL, "123456")));

        assertEquals(ErrorCode.SIGNUP_CODE_BLOCKED, e.getErrorCode());
        verify(store, never()).consumeCode(anyString(), anyString());

    }

    @Test
    @DisplayName("이메일 인증을 마치지 않으면 가입할 수 없다.")
    void rejectsSignupWithoutVerification() {

        when(store.isVerified(EMAIL)).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class, () -> authService.signup(signup()));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, e.getErrorCode());
        verify(memberRepository, never()).save(org.mockito.ArgumentMatchers.any(Member.class));

    }

    @Test
    @DisplayName("가입을 마치면 인증 기록을 지워 같은 인증으로 다시 가입하지 못하게 한다.")
    void clearsVerificationAfterSignup() {

        when(store.isVerified(EMAIL)).thenReturn(true);

        authService.signup(signup());

        verify(store).clear(EMAIL);

    }

}
