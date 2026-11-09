package com.mealgram.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BirthDateValidatorTest {

    private final BirthDateValidator validator = new BirthDateValidator();

    private boolean valid(LocalDate birthDate) {

        return validator.isValid(birthDate, null);

    }

    @Test
    @DisplayName("생년월일이 없으면 통과한다.")
    void nullIsValid() {

        assertTrue(valid(null));

    }

    @Test
    @DisplayName("오늘 기준 만 3세 생일 당일부터 허용하고 하루 모자라면 막는다.")
    void minimumAgeByDay() {

        LocalDate today = LocalDate.now();

        assertTrue(valid(today.minusYears(3)));
        assertFalse(valid(today.minusYears(3).plusDays(1)));

    }

    @Test
    @DisplayName("만 120세는 허용하고 만 121세가 되는 날부터 막는다.")
    void maximumAgeByDay() {

        LocalDate today = LocalDate.now();

        assertTrue(valid(today.minusYears(120)));
        assertTrue(valid(today.minusYears(121).plusDays(1)));
        assertFalse(valid(today.minusYears(121)));

    }

    @Test
    @DisplayName("미래 날짜는 막는다.")
    void futureIsInvalid() {

        assertFalse(valid(LocalDate.now().plusDays(1)));

    }

}
