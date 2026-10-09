package com.mealgram.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 비밀번호 재설정 요청 데이터

public record ResetPasswordRequest(

    @NotBlank
    String token,

    @NotBlank
    @Size(min=8, max=20)
    String newPassword

) {}
