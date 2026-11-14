package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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
import com.mealgram.member.dto.ForgotPasswordRequest;
import com.mealgram.member.dto.LoginRequest;
import com.mealgram.member.dto.LoginResponse;
import com.mealgram.member.dto.LogoutRequest;
import com.mealgram.member.dto.RefreshRequest;
import com.mealgram.member.dto.RefreshResponse;
import com.mealgram.member.dto.ResetPasswordRequest;
import com.mealgram.member.dto.VerifyResetCodeRequest;
import com.mealgram.member.dto.VerifyResetCodeResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class AuthServiceTest {

    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenStore refreshTokenStore;
    private PasswordResetTokenStore passwordResetTokenStore;
    private PasswordResetCodeStore passwordResetCodeStore;
    private PasswordResetMailer passwordResetMailer;
    private JwtTokenProvider provider;
    private AuthService authService;

    @BeforeEach
    void setUp() {

        memberRepository = mock(MemberRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        refreshTokenStore = mock(RefreshTokenStore.class);
        provider = new JwtTokenProvider(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));
        passwordResetTokenStore = mock(PasswordResetTokenStore.class);
        passwordResetCodeStore = mock(PasswordResetCodeStore.class);
        passwordResetMailer = mock(PasswordResetMailer.class);
        authService = new AuthService(memberRepository, passwordEncoder, provider, refreshTokenStore,
                passwordResetTokenStore, passwordResetCodeStore, passwordResetMailer, mock(LoginAttemptStore.class));

    }

    private void stored(Long memberId, String token) {

        when(refreshTokenStore.find(memberId)).thenReturn(Optional.ofNullable(token));

    }

    @Test
    @DisplayName("로그인하면 refreshToken을 보관한다.")
    void savesRefreshTokenOnLogin() {

        Member member = Member.builder().id(1L).loginId("hangyeol01").password("encoded").build();
        when(memberRepository.findByLoginId("hangyeol01")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password1", "encoded")).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest("hangyeol01", "password1"));

        assertTrue(provider.isAccessToken(response.accessToken()));
        assertTrue(provider.isRefreshToken(response.refreshToken()));
        verify(refreshTokenStore).save(1L, response.refreshToken());

    }

    @Test
    @DisplayName("보관된 refreshToken과 같으면 accessToken을 새로 준다.")
    void issuesNewAccessToken() {

        String refresh = provider.generateRefreshToken(1L);
        stored(1L, refresh);
        when(memberRepository.existsById(1L)).thenReturn(true);

        RefreshResponse response = authService.refresh(new RefreshRequest(refresh));

        assertTrue(provider.isAccessToken(response.accessToken()));
        assertEquals(1L, provider.getMemberId(response.accessToken()));

    }

    @Test
    @DisplayName("accessToken으로는 재발급할 수 없다.")
    void rejectsAccessTokenForRefresh() {

        String access = provider.generateAccessToken(1L);
        stored(1L, access);
        when(memberRepository.existsById(1L)).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> authService.refresh(new RefreshRequest(access)));

        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, e.getErrorCode());

    }

    @Test
    @DisplayName("보관된 값이 없거나 다르면 재발급할 수 없다.")
    void rejectsUnknownOrReplacedToken() {

        String refresh = provider.generateRefreshToken(1L);
        when(memberRepository.existsById(1L)).thenReturn(true);

        stored(1L, null);
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN,
                assertThrows(BusinessException.class, () -> authService.refresh(new RefreshRequest(refresh))).getErrorCode());

        stored(1L, "다른 기기에서 로그인해 교체된 토큰");
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN,
                assertThrows(BusinessException.class, () -> authService.refresh(new RefreshRequest(refresh))).getErrorCode());

    }

    @Test
    @DisplayName("형식이 틀린 토큰은 재발급할 수 없다.")
    void rejectsBrokenToken() {

        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN,
                assertThrows(BusinessException.class, () -> authService.refresh(new RefreshRequest("broken"))).getErrorCode());

    }

    @Test
    @DisplayName("탈퇴한 회원의 토큰은 재발급할 수 없다.")
    void rejectsTokenOfDeletedMember() {

        String refresh = provider.generateRefreshToken(1L);
        stored(1L, refresh);
        when(memberRepository.existsById(1L)).thenReturn(false);

        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN,
                assertThrows(BusinessException.class, () -> authService.refresh(new RefreshRequest(refresh))).getErrorCode());

    }

    @Test
    @DisplayName("로그아웃하면 보관된 refreshToken을 지운다.")
    void deletesStoredTokenOnLogout() {

        String refresh = provider.generateRefreshToken(1L);
        stored(1L, refresh);

        authService.logout(new LogoutRequest(refresh));

        verify(refreshTokenStore).delete(1L);

    }

    @Test
    @DisplayName("보관된 값과 다르거나 틀린 토큰으로 로그아웃해도 오류 없이 아무것도 지우지 않는다.")
    void logoutWithInvalidTokenDoesNothing() {

        String refresh = provider.generateRefreshToken(1L);
        stored(1L, "교체된 토큰");

        authService.logout(new LogoutRequest(refresh));
        authService.logout(new LogoutRequest("broken"));
        authService.logout(new LogoutRequest(provider.generateAccessToken(1L)));

        verify(refreshTokenStore, never()).delete(anyLong());
        verify(memberRepository, never()).findById(any());

    }

    @Test
    @DisplayName("가입된 이메일이면 인증번호를 보관하고 메일을 보낸다.")
    void savesCodeAndSendsMail() {

        Member member = Member.builder().id(1L).email("a@example.com").build();
        when(memberRepository.findByEmail("a@example.com")).thenReturn(Optional.of(member));
        when(passwordResetTokenStore.acquireCooldown(1L)).thenReturn(true);

        authService.forgotPassword(new ForgotPasswordRequest("a@example.com"));

        ArgumentCaptor<String> saved = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(passwordResetCodeStore).save(eq(1L), saved.capture());
        verify(passwordResetMailer).send(eq("a@example.com"), sent.capture());
        assertEquals(saved.getValue(), sent.getValue());
        assertTrue(saved.getValue().matches("\\d{6}"));

    }

    @Test
    @DisplayName("인증번호는 항상 숫자 6자리로 만든다.")
    void createsSixDigitCodes() {

        Member member = Member.builder().id(1L).email("a@example.com").build();
        when(memberRepository.findByEmail("a@example.com")).thenReturn(Optional.of(member));
        when(passwordResetTokenStore.acquireCooldown(1L)).thenReturn(true);

        for (int i = 0; i < 20; i++) {
            authService.forgotPassword(new ForgotPasswordRequest("a@example.com"));
        }

        ArgumentCaptor<String> codes = ArgumentCaptor.forClass(String.class);
        verify(passwordResetCodeStore, org.mockito.Mockito.times(20)).save(eq(1L), codes.capture());
        assertTrue(codes.getAllValues().stream().allMatch(code -> code.matches("\\d{6}")));

    }

    @Test
    @DisplayName("가입되지 않은 이메일이어도 오류 없이 아무것도 하지 않는다.")
    void ignoresUnknownEmail() {

        when(memberRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("none@example.com"));

        verify(passwordResetCodeStore, never()).save(anyLong(), anyString());
        verify(passwordResetMailer, never()).send(anyString(), anyString());

    }

    @Test
    @DisplayName("방금 요청한 회원에게는 메일을 다시 보내지 않는다.")
    void skipsDuringCooldown() {

        Member member = Member.builder().id(1L).email("a@example.com").build();
        when(memberRepository.findByEmail("a@example.com")).thenReturn(Optional.of(member));
        when(passwordResetTokenStore.acquireCooldown(1L)).thenReturn(false);

        authService.forgotPassword(new ForgotPasswordRequest("a@example.com"));

        verify(passwordResetCodeStore, never()).save(anyLong(), anyString());
        verify(passwordResetMailer, never()).send(anyString(), anyString());

    }

    @Test
    @DisplayName("맞는 인증번호면 재설정 토큰을 보관하고 돌려준다.")
    void verifiesCode() {

        Member member = Member.builder().id(1L).email("a@example.com").build();
        when(memberRepository.findByEmail("a@example.com")).thenReturn(Optional.of(member));
        when(passwordResetCodeStore.consume(1L, "123456")).thenReturn(true);

        VerifyResetCodeResponse response = authService.verifyResetCode(new VerifyResetCodeRequest("A@Example.com", "123456"));

        assertEquals(43, response.resetToken().length());
        verify(passwordResetTokenStore).save(1L, response.resetToken());
        verify(passwordResetCodeStore).resetFailures("a@example.com");

    }

    @Test
    @DisplayName("틀린 인증번호면 실패를 기록하고 토큰을 만들지 않는다.")
    void rejectsWrongCode() {

        Member member = Member.builder().id(1L).email("a@example.com").build();
        when(memberRepository.findByEmail("a@example.com")).thenReturn(Optional.of(member));
        when(passwordResetCodeStore.consume(1L, "000000")).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.verifyResetCode(new VerifyResetCodeRequest("a@example.com", "000000")));

        assertEquals(ErrorCode.INVALID_RESET_CODE, e.getErrorCode());
        verify(passwordResetCodeStore).recordFailure("a@example.com");
        verify(passwordResetTokenStore, never()).save(anyLong(), anyString());

    }

    @Test
    @DisplayName("가입되지 않은 이메일도 틀린 인증번호와 같은 오류로 응답한다.")
    void rejectsUnknownEmailLikeWrongCode() {

        when(memberRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.verifyResetCode(new VerifyResetCodeRequest("none@example.com", "123456")));

        assertEquals(ErrorCode.INVALID_RESET_CODE, e.getErrorCode());
        verify(passwordResetCodeStore).recordFailure("none@example.com");
        verify(passwordResetCodeStore, never()).consume(anyLong(), anyString());

    }

    @Test
    @DisplayName("틀린 횟수가 많으면 인증번호를 확인하지 않고 막는다.")
    void blocksAfterRepeatedFailures() {

        when(passwordResetCodeStore.isBlocked("a@example.com")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.verifyResetCode(new VerifyResetCodeRequest("a@example.com", "123456")));

        assertEquals(ErrorCode.RESET_CODE_BLOCKED, e.getErrorCode());
        verify(memberRepository, never()).findByEmail(anyString());

    }

    @Test
    @DisplayName("유효한 토큰이면 새 비밀번호로 바꾸고 refreshToken을 지운다.")
    void resetsPassword() {

        Member member = Member.builder().id(1L).password("old").build();
        when(passwordResetTokenStore.consume("token")).thenReturn(Optional.of(1L));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("newPassword1")).thenReturn("ENCODED");

        authService.resetPassword(new ResetPasswordRequest("token", "newPassword1"));

        assertEquals("ENCODED", member.getPassword());
        verify(memberRepository).save(member);
        verify(refreshTokenStore).delete(1L);

    }

    @Test
    @DisplayName("유효하지 않거나 이미 쓴 토큰이면 비밀번호를 바꿀 수 없다.")
    void rejectsInvalidResetToken() {

        when(passwordResetTokenStore.consume("used")).thenReturn(Optional.empty());

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("used", "newPassword1")));

        assertEquals(ErrorCode.INVALID_RESET_TOKEN, e.getErrorCode());
        verify(memberRepository, never()).save(any());

    }

    @Test
    @DisplayName("토큰의 회원이 탈퇴했으면 비밀번호를 바꿀 수 없다.")
    void rejectsTokenOfDeletedMemberOnReset() {

        when(passwordResetTokenStore.consume("token")).thenReturn(Optional.of(9L));
        when(memberRepository.findById(9L)).thenReturn(Optional.empty());

        BusinessException e = assertThrows(BusinessException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("token", "newPassword1")));

        assertEquals(ErrorCode.INVALID_RESET_TOKEN, e.getErrorCode());

    }

}
