package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.mealgram.common.security.PasswordResetTokenStore;
import com.mealgram.common.security.RefreshTokenStore;
import com.mealgram.member.dto.ForgotPasswordRequest;
import com.mealgram.member.dto.SignupRequest;

class EmailNormalizationTest {

    private MemberRepository memberRepository;
    private PasswordResetTokenStore passwordResetTokenStore;
    private PasswordResetMailer passwordResetMailer;
    private AuthService authService;

    @BeforeEach
    void setUp() {

        memberRepository = mock(MemberRepository.class);
        passwordResetTokenStore = mock(PasswordResetTokenStore.class);
        passwordResetMailer = mock(PasswordResetMailer.class);
        authService = new AuthService(memberRepository, mock(PasswordEncoder.class), mock(JwtTokenProvider.class),
                mock(RefreshTokenStore.class), passwordResetTokenStore, passwordResetMailer);

    }

    private SignupRequest signup(String email) {

        return new SignupRequest("한결", "hangyeol01", "password12!", null, null, null, null, null, email);

    }

    @Test
    @DisplayName("가입할 때 이메일을 앞뒤 공백 없이 소문자로 저장한다.")
    void storesLowercaseEmail() {

        authService.signup(signup(" Han.Gyeol@Example.COM "));

        ArgumentCaptor<Member> saved = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(saved.capture());
        assertEquals("han.gyeol@example.com", saved.getValue().getEmail());
        verify(memberRepository).existsByEmail("han.gyeol@example.com");

    }

    @Test
    @DisplayName("대소문자만 다른 이메일로 다시 가입하면 중복으로 막는다.")
    void rejectsEmailDifferingOnlyByCase() {

        when(memberRepository.existsByEmail("han@example.com")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> authService.signup(signup("HAN@Example.com")));

        assertEquals(ErrorCode.DUPLICATE_EMAIL, e.getErrorCode());
        verify(memberRepository, never()).save(any());

    }

    @Test
    @DisplayName("비밀번호 찾기에서 대소문자가 달라도 같은 이메일로 찾는다.")
    void findsMemberRegardlessOfCase() {

        Member member = Member.builder().id(1L).email("han@example.com").build();
        when(memberRepository.findByEmail("han@example.com")).thenReturn(Optional.of(member));
        when(passwordResetTokenStore.acquireCooldown(1L)).thenReturn(true);

        authService.forgotPassword(new ForgotPasswordRequest("Han@EXAMPLE.com"));

        verify(passwordResetTokenStore).save(anyLong(), anyString());
        verify(passwordResetMailer).send(anyString(), anyString());

    }

}
