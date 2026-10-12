package com.mealgram.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// 비밀번호 규칙 검증(KISA 패스워드 작성 규칙 기준)

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MAX_LENGTH = 20;
    private static final int MIN_LENGTH_THREE_TYPES = 8;
    private static final int MIN_LENGTH_TWO_TYPES = 10;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if (password == null) {
            return true;
        }

        int length = password.length();
        if (length > MAX_LENGTH || password.chars().anyMatch(Character::isWhitespace)) {
            return false;
        }

        boolean hasLetter = password.chars().anyMatch(c -> isAsciiLetter(c));
        boolean hasDigit = password.chars().anyMatch(c -> c >= '0' && c <= '9');
        boolean hasSpecial = password.chars().anyMatch(c -> c > 32 && c < 127 && !isAsciiLetter(c) && !(c >= '0' && c <= '9'));

        int types = (hasLetter ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        if (types >= 3) {
            return length >= MIN_LENGTH_THREE_TYPES;
        }

        return types == 2 && length >= MIN_LENGTH_TWO_TYPES;

    }

    private boolean isAsciiLetter(int c) {

        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');

    }

}
