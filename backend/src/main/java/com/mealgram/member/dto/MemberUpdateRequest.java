package com.mealgram.member.dto;

import java.math.BigDecimal;

import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// 내정보 수정 요청 데이터

public record MemberUpdateRequest(

    @Size(min=2, max=10)
    String nickname,

    @Positive
    Integer age,

    Gender gender,

    @Positive
    BigDecimal height,

    @Positive
    BigDecimal weight,

    ActivityLevel activityLevel

) {}
