package com.mealgram.member.dto;

// 로그인 응답 데이터

public record LoginResponse(

    String accessToken,

    String refreshToken

) {}
