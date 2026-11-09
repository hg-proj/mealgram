package com.mealgram.meal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

// 비회원 목표 열량 계산용 신체 정보

public record BodyInfo(LocalDate birthDate, Gender gender, BigDecimal height, BigDecimal weight,
                       ActivityLevel activityLevel) {

    // 만 나이, 생년월일이 없으면 null
    public Integer ageOn(LocalDate today) {

        return birthDate == null ? null : Period.between(birthDate, today).getYears();

    }

}
