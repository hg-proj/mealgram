package com.mealgram.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 비밀번호 재설정 인증번호 확인 요청 데이터

public record VerifyResetCodeRequest(

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Pattern(regexp="^[0-9]{6}$")
    String code

) {}
