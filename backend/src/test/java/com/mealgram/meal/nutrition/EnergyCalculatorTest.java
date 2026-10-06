package com.mealgram.meal.nutrition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

class EnergyCalculatorTest {

    @Test
    @DisplayName("성인 여성 공식으로 계산한다.")
    void calculatesAdultFemaleByFormula() {

        int result = EnergyCalculator.dailyCalorie(30, Gender.FEMALE, new BigDecimal("160"),
                new BigDecimal("55"), ActivityLevel.LIGHT);

        assertEquals(2024, result);

    }

    @Test
    @DisplayName("성인 남성 공식으로 계산한다.")
    void calculatesAdultMaleByFormula() {

        int result = EnergyCalculator.dailyCalorie(25, Gender.MALE, new BigDecimal("175"),
                new BigDecimal("70"), ActivityLevel.LIGHT);

        assertEquals(2708, result);

    }

    @Test
    @DisplayName("청소년 공식으로 계산한다.")
    void calculatesChildByFormula() {

        int result = EnergyCalculator.dailyCalorie(10, Gender.MALE, new BigDecimal("140"),
                new BigDecimal("35"), ActivityLevel.LIGHT);

        assertEquals(1979, result);

    }

    @Test
    @DisplayName("키와 몸무게가 없으면 사전 계산표를 쓴다.")
    void usesReferenceTableWithoutBodyInfo() {

        int result = EnergyCalculator.dailyCalorie(30, Gender.FEMALE, null, null, null);

        assertEquals(1900, result);

    }

    @Test
    @DisplayName("정보가 비어 있으면 기본값을 쓴다.")
    void usesDefaultWithoutAgeOrGender() {

        int result = EnergyCalculator.dailyCalorie(null, Gender.FEMALE, new BigDecimal("160"),
                new BigDecimal("55"), ActivityLevel.LIGHT);

        assertEquals(EnergyCalculator.DEFAULT_DAILY_CALORIE, result);

    }

    @Test
    @DisplayName("한 끼 목표는 하루의 삼분의 일이고 목표로 가감한다.")
    void appliesGoalAdjustmentToMealCalorie() {

        assertEquals(667, EnergyCalculator.mealCalorie(2000, null));
        assertEquals(667, EnergyCalculator.mealCalorie(2000, "유지"));
        assertEquals(500, EnergyCalculator.mealCalorie(2000, "다이어트"));
        assertEquals(767, EnergyCalculator.mealCalorie(2000, "벌크업"));

    }

}
