package com.mealgram.member.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

import com.mealgram.common.validation.ValidBirthDate;
import com.mealgram.common.validation.ValidPassword;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @ValidPassword
    String password,

    @ValidBirthDate
    LocalDate birthDate,

    Gender gender,

    @DecimalMin("50")
    @DecimalMax("250")
    BigDecimal height,

    @DecimalMin("10")
    @DecimalMax("300")
    BigDecimal weight,

    ActivityLevel activityLevel,

    @NotBlank
    @Email
    String email

) {}
