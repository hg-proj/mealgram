package com.mealgram.member;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.mealgram.member.dto.SignupRequest;
import com.mealgram.member.dto.SignupResponse;
import com.mealgram.member.dto.VerifyResetCodeRequest;
import com.mealgram.member.dto.VerifyResetCodeResponse;

// 회원가입, 로그인, 토큰 재발급, 로그아웃, 비밀번호 재설정 처리 서비스

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final PasswordResetCodeStore passwordResetCodeStore;
    private final PasswordResetMailer passwordResetMailer;
    private final LoginAttemptStore loginAttemptStore;
    private final String dummyHash;

    private static final String DUMMY_PASSWORD = "mealgram-dummy-password";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RESET_CODE_BOUND = 1_000_000;
    private static final int RESET_TOKEN_BYTES = 32;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, RefreshTokenStore refreshTokenStore,
                       PasswordResetTokenStore passwordResetTokenStore, PasswordResetCodeStore passwordResetCodeStore,
                       PasswordResetMailer passwordResetMailer, LoginAttemptStore loginAttemptStore) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.passwordResetTokenStore = passwordResetTokenStore;
        this.passwordResetCodeStore = passwordResetCodeStore;
        this.passwordResetMailer = passwordResetMailer;
        this.loginAttemptStore = loginAttemptStore;
        this.dummyHash = passwordEncoder.encode(DUMMY_PASSWORD);
    }

    public SignupResponse signup(SignupRequest request) {
        
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        String email = normalizeEmail(request.email());

        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .nickname(request.nickname())
                .loginId(request.loginId())
                .password(encodedPassword)
                .birthDate(request.birthDate())
                .gender(request.gender())
                .height(request.height())
                .weight(request.weight())
                .activityLevel(request.activityLevel())
                .email(email)
                .build();

        memberRepository.save(member);

        return new SignupResponse(member.getId());
    }

    public LoginResponse login(LoginRequest request) {

        String loginId = request.loginId();

        if (loginAttemptStore.isBlocked(loginId)) {
            throw new BusinessException(ErrorCode.LOGIN_BLOCKED);
        }

        Member member = memberRepository.findByLoginId(loginId).orElse(null);

        String hash = member == null ? dummyHash : member.getPassword();
        boolean matched = passwordEncoder.matches(request.password(), hash);

        if (member == null || !matched) {
            loginAttemptStore.recordFailure(loginId);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        loginAttemptStore.reset(loginId);

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

        memberRepository.findByEmail(normalizeEmail(request.email())).ifPresent(member -> {
            if (!passwordResetTokenStore.acquireCooldown(member.getId())) {
                return;
            }

            String code = generateResetCode();
            passwordResetCodeStore.save(member.getId(), code);
            passwordResetMailer.send(member.getEmail(), code);
        });
    }

    public VerifyResetCodeResponse verifyResetCode(VerifyResetCodeRequest request) {

        String email = normalizeEmail(request.email());

        if (passwordResetCodeStore.isBlocked(email)) {
            throw new BusinessException(ErrorCode.RESET_CODE_BLOCKED);
        }

        Member member = memberRepository.findByEmail(email).orElse(null);

        if (member == null || !passwordResetCodeStore.consume(member.getId(), request.code())) {
            passwordResetCodeStore.recordFailure(email);
            throw new BusinessException(ErrorCode.INVALID_RESET_CODE);
        }

        passwordResetCodeStore.resetFailures(email);

        String token = generateResetToken();
        passwordResetTokenStore.save(member.getId(), token);

        return new VerifyResetCodeResponse(token);
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

    private String normalizeEmail(String email) {

        return email.strip().toLowerCase(Locale.ROOT);

    }

    private String generateResetCode() {

        return String.format("%06d", RANDOM.nextInt(RESET_CODE_BOUND));

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
