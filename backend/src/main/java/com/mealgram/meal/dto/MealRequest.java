package com.mealgram.meal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.mealgram.common.validation.ValidBirthDate;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 식단 추천 요청 데이터

public record MealRequest(Long requiredId,
                          @Size(max = 20) List<Long> subIds,
                          Long ingredientId,
                          @Pattern(regexp = "다이어트|벌크업|유지", message = "목표는 다이어트, 벌크업, 유지 중 하나여야 합니다.") String goal,
                          @Size(max = 50) String genre,
                          @ValidBirthDate LocalDate birthDate,
                          Gender gender,
                          @DecimalMin("50") @DecimalMax("250") BigDecimal height,
                          @DecimalMin("10") @DecimalMax("300") BigDecimal weight,
                          ActivityLevel activityLevel) {

    public BodyInfo bodyInfo() {

        return new BodyInfo(birthDate, gender, height, weight, activityLevel);

    }

}
