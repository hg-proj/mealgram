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
                          @Size(max = 100) List<Long> subIds,
                          Long ingredientId,
                          @Pattern(regexp = "다이어트|벌크업|유지|랜덤", message = "목표는 다이어트, 벌크업, 유지, 랜덤 중 하나여야 합니다.") String goal,
                          @Size(max = 50) String genre,
                          @Pattern(regexp = "균형|고단백|저탄수|저지방", message = "식단 성향은 균형, 고단백, 저탄수, 저지방 중 하나여야 합니다.") String style,
                          @ValidBirthDate LocalDate birthDate,
                          Gender gender,
                          @DecimalMin("50") @DecimalMax("250") BigDecimal height,
                          @DecimalMin("10") @DecimalMax("300") BigDecimal weight,
                          ActivityLevel activityLevel) {

    public BodyInfo bodyInfo() {

        return new BodyInfo(birthDate, gender, height, weight, activityLevel);

    }

}
