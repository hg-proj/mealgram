package com.mealgram.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.security.JwtTokenProvider;
import com.mealgram.common.security.RefreshTokenStore;
import com.mealgram.member.dto.LoginRequest;
import com.mealgram.member.dto.LoginResponse;
import com.mealgram.member.dto.RefreshRequest;
import com.mealgram.member.dto.RefreshResponse;
import com.mealgram.member.dto.SignupRequest;
import com.mealgram.member.dto.SignupResponse;

// 회원가입, 로그인, 토큰 재발급 처리 서비스

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, RefreshTokenStore refreshTokenStore) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
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

    private Long findStoredMemberId(String refreshToken) {

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            return null;
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        return refreshTokenStore.find(memberId).filter(refreshToken::equals).isPresent() ? memberId : null;
    }

}
