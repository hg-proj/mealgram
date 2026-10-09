package com.mealgram.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 비밀번호 재설정 메일 요청 데이터

public record ForgotPasswordRequest(@NotBlank @Email String email) {

}
