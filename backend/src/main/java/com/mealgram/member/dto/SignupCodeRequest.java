package com.mealgram.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 회원가입 이메일 인증번호 요청 데이터

// 닉네임은 메일 인사말에만 쓰는 선택 값
public record SignupCodeRequest(@NotBlank @Email @Size(max = 255) String email, @Size(max = 10) String nickname) {
}
