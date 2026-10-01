package com.mealgram.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.security.JwtTokenProvider;
import com.mealgram.member.dto.LoginRequest;
import com.mealgram.member.dto.LoginResponse;
import com.mealgram.member.dto.SignupRequest;
import com.mealgram.member.dto.SignupResponse;

// 회원가입/로그인 처리 서비스

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
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

        return new LoginResponse(accessToken, refreshToken);
    }

}
