package com.mealgram.member;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.mail.PasswordResetMailer;
import com.mealgram.common.security.JwtTokenProvider;
import com.mealgram.common.security.PasswordResetTokenStore;
import com.mealgram.common.security.RefreshTokenStore;
import com.mealgram.member.dto.ForgotPasswordRequest;
import com.mealgram.member.dto.LoginRequest;
import com.mealgram.member.dto.LoginResponse;
import com.mealgram.member.dto.LogoutRequest;
import com.mealgram.member.dto.RefreshRequest;
import com.mealgram.member.dto.RefreshResponse;
import com.mealgram.member.dto.ResetPasswordRequest;
import com.mealgram.member.dto.SignupRequest;
import com.mealgram.member.dto.SignupResponse;

// 회원가입, 로그인, 토큰 재발급, 로그아웃, 비밀번호 재설정 처리 서비스

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final PasswordResetMailer passwordResetMailer;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTES = 32;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, RefreshTokenStore refreshTokenStore,
                       PasswordResetTokenStore passwordResetTokenStore, PasswordResetMailer passwordResetMailer) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.passwordResetTokenStore = passwordResetTokenStore;
        this.passwordResetMailer = passwordResetMailer;
    }

    public SignupResponse signup(SignupRequest request) {
        
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .nickname(request.nickname())
                .loginId(request.loginId())
                .password(encodedPassword)
                .age(request.age())
                .gender(request.gender())
                .height(request.height())
                .weight(request.weight())
                .activityLevel(request.activityLevel())
                .email(request.email())
                .build();

        memberRepository.save(member);

        return new SignupResponse(member.getId());
    }

    public LoginResponse login(LoginRequest request) {

        Member member = memberRepository.findByLoginId(request.loginId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
                        
        if (!passwordEncoder.matches(request.password(), member.getPassword())){
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId());
        refreshTokenStore.save(member.getId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    public RefreshResponse refresh(RefreshRequest request) {

        Long memberId = findStoredMemberId(request.refreshToken());
        if (memberId == null || !memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return new RefreshResponse(jwtTokenProvider.generateAccessToken(memberId));
    }

    public void logout(LogoutRequest request) {

        Long memberId = findStoredMemberId(request.refreshToken());
        if (memberId != null) {
            refreshTokenStore.delete(memberId);
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {

        memberRepository.findByEmail(request.email()).ifPresent(member -> {
            if (!passwordResetTokenStore.acquireCooldown(member.getId())) {
                return;
            }

            String token = generateResetToken();
            passwordResetTokenStore.save(member.getId(), token);
            passwordResetMailer.send(member.getEmail(), token);
        });
    }

    public void resetPassword(ResetPasswordRequest request) {

        Long memberId = passwordResetTokenStore.consume(request.token())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));

        member.changePassword(passwordEncoder.encode(request.newPassword()));
        memberRepository.save(member);
        refreshTokenStore.delete(memberId);
    }

    private String generateResetToken() {

        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Long findStoredMemberId(String refreshToken) {

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            return null;
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        return refreshTokenStore.find(memberId).filter(refreshToken::equals).isPresent() ? memberId : null;
    }

}
