package com.mealgram.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.member.dto.SignupRequest;
import com.mealgram.member.dto.SignupResponse;

// 회원가입/로그인 처리 서비스

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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

}
