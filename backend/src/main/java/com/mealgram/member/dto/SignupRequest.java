package com.mealgram.member.dto;

import java.math.BigDecimal;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// 회원가입 요청 데이터

public record SignupRequest (

    @Size(min=2, max=10)
    @NotBlank
    String nickname,

    @NotBlank
    @Size(min=6, max=20)
    @Pattern(regexp="^[a-z0-9]+$")
    String loginId,

    @NotBlank
    @Size(min=8, max=20)
    String password,

    @Positive
    Integer age,

    Gender gender,

    @Positive
    BigDecimal height,

    @Positive
    BigDecimal weight,

    ActivityLevel activityLevel,

    @NotBlank
    @Email
    String email

) {}
