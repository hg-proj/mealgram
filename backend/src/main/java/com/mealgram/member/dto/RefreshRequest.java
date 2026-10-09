package com.mealgram.member.dto;

import jakarta.validation.constraints.NotBlank;

// accessToken 재발급 요청 데이터

public record RefreshRequest(@NotBlank String refreshToken) {

}
