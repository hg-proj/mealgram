package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.mealgram.common.security.JwtTokenProvider;
import com.mealgram.common.security.RefreshTokenStore;
import com.mealgram.member.dto.LoginRequest;
import com.mealgram.member.dto.LoginResponse;
import com.mealgram.member.dto.LogoutRequest;
import com.mealgram.member.dto.RefreshRequest;
import com.mealgram.member.dto.RefreshResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class AuthServiceTest {

    private MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenStore refreshTokenStore;
    private JwtTokenProvider provider;
    private AuthService authService;

    @BeforeEach
    void setUp() {

        memberRepository = mock(MemberRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        refreshTokenStore = mock(RefreshTokenStore.class);
        provider = new JwtTokenProvider(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));
        authService = new AuthService(memberRepository, passwordEncoder, provider, refreshTokenStore);

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

}
