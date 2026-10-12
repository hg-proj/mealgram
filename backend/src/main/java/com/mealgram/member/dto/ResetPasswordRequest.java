package com.mealgram.member.dto;

import com.mealgram.common.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;

// 비밀번호 재설정 요청 데이터

public record ResetPasswordRequest(

    @NotBlank
    String token,

    @NotBlank
    @ValidPassword
    String newPassword

) {}
