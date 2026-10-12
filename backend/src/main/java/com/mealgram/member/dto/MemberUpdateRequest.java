package com.mealgram.member.dto;

import java.math.BigDecimal;

import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 내정보 수정 요청 데이터

public record MemberUpdateRequest(

    @Size(min=2, max=10)
    @Pattern(regexp=".*[^ \t\r\n].*", message="공백만 입력할 수 없습니다.")
    String nickname,

    @Min(3)
    @Max(120)
    Integer age,

    Gender gender,

    @DecimalMin("50")
    @DecimalMax("250")
    BigDecimal height,

    @DecimalMin("10")
    @DecimalMax("300")
    BigDecimal weight,

    ActivityLevel activityLevel

) {}
