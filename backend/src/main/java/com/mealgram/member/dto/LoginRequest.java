package com.mealgram.member.dto;

import jakarta.validation.constraints.NotBlank;

// 로그인 요청 데이터

public record LoginRequest(

    @NotBlank
    String loginId,

    @NotBlank
    String password

) {}
