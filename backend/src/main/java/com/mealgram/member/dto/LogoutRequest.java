package com.mealgram.member.dto;

import jakarta.validation.constraints.NotBlank;

// 로그아웃 요청 데이터

public record LogoutRequest(@NotBlank String refreshToken) {

}
