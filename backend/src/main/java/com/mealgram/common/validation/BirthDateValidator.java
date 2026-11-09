package com.mealgram.common.validation;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// 생년월일 검증, 오늘 기준 만 나이가 3세 이상 120세 이하인지 확인

public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {

    private static final int MIN_AGE = 3;
    private static final int MAX_AGE = 120;

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {

        if (birthDate == null) {
            return true;
        }

        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            return false;
        }

        int age = Period.between(birthDate, today).getYears();

        return age >= MIN_AGE && age <= MAX_AGE;

    }

}
