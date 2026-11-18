package com.mealgram.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 회원가입 이메일 인증번호 확인 요청 데이터

public record SignupCodeVerifyRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$") String code) {
}
