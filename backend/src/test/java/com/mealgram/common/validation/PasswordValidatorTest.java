package com.mealgram.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    private boolean valid(String password) {

        return validator.isValid(password, null);

    }

    @Test
    @DisplayName("영문, 숫자, 특수문자 3종류면 8자부터 허용한다.")
    void threeTypesFromEightChars() {

        assertTrue(valid("abcd12!@"));
        assertFalse(valid("abc12!@"));

    }

    @Test
    @DisplayName("2종류만 쓰면 10자 이상이어야 한다.")
    void twoTypesNeedTenChars() {

        assertFalse(valid("abcdefg12"));
        assertTrue(valid("abcdefg123"));
        assertTrue(valid("abcdefghi!"));
        assertTrue(valid("1234567!@#"));

    }

    @Test
    @DisplayName("1종류만 쓰면 길이와 상관없이 허용하지 않는다.")
    void oneTypeIsRejected() {

        assertFalse(valid("aaaaaaaaaaaaaaaa"));
        assertFalse(valid("1234567890123456"));
        assertFalse(valid("!!!!!!!!!!!!!!!!"));

    }

    @Test
    @DisplayName("20자를 넘거나 공백이 있으면 허용하지 않는다.")
    void rejectsTooLongOrWhitespace() {

        assertTrue(valid("abcdefghij1234567!@#"));
        assertFalse(valid("abcdefghij1234567!@#a"));
        assertFalse(valid("abcd 123!@"));
        assertFalse(valid("        "));

    }

    @Test
    @DisplayName("한글은 종류로 세지 않는다.")
    void koreanDoesNotCountAsType() {

        assertFalse(valid("가나다라마바사아자차"));
        assertFalse(valid("가나다라마바사아1"));
        assertTrue(valid("가나다라1a!마바사"));

    }

    @Test
    @DisplayName("값이 없으면 통과시킨다(필수 여부는 NotBlank가 검사).")
    void nullIsLeftToNotBlank() {

        assertTrue(valid(null));

    }

}
