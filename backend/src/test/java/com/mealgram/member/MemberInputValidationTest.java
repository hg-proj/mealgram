package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.member.dto.MemberUpdateRequest;
import com.mealgram.member.dto.ResetPasswordRequest;
import com.mealgram.member.dto.SignupRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class MemberInputValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {

        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

    }

    @AfterAll
    static void tearDown() {

        factory.close();

    }

    private SignupRequest signup(String password, Integer age, String height, String weight) {

        return new SignupRequest("한결", "hangyeol01", password, age, null,
                height == null ? null : new BigDecimal(height), weight == null ? null : new BigDecimal(weight),
                null, "test@example.com");

    }

    private MemberUpdateRequest update(String nickname, Integer age, String height, String weight) {

        return new MemberUpdateRequest(nickname, age, null, height == null ? null : new BigDecimal(height),
                weight == null ? null : new BigDecimal(weight), null);

    }

    private <T> Set<String> fields(T target) {

        Set<String> fields = new TreeSet<>();
        for (ConstraintViolation<T> violation : validator.validate(target)) {
            fields.add(violation.getPropertyPath().toString());
        }

        return fields;

    }

    @Test
    @DisplayName("가입: 규칙에 맞는 값은 통과한다.")
    void signupValid() {

        assertTrue(fields(signup("password12!", 30, "160", "55")).isEmpty());
        assertTrue(fields(signup("password12!", null, null, null)).isEmpty());

    }

    @Test
    @DisplayName("가입: 나이, 키, 몸무게가 범위를 벗어나면 막는다.")
    void signupOutOfRange() {

        assertEquals(Set.of("age"), fields(signup("password12!", 2, "160", "55")));
        assertEquals(Set.of("age"), fields(signup("password12!", 121, "160", "55")));
        assertEquals(Set.of("height"), fields(signup("password12!", 30, "49.9", "55")));
        assertEquals(Set.of("height"), fields(signup("password12!", 30, "251", "55")));
        assertEquals(Set.of("weight"), fields(signup("password12!", 30, "160", "9.9")));
        assertEquals(Set.of("weight"), fields(signup("password12!", 30, "160", "300.1")));

    }

    @Test
    @DisplayName("가입: 3세, 120세와 경계값은 허용한다.")
    void signupBoundaries() {

        assertTrue(fields(signup("password12!", 3, "50", "10")).isEmpty());
        assertTrue(fields(signup("password12!", 120, "250", "300")).isEmpty());

    }

    @Test
    @DisplayName("가입: 비밀번호 규칙을 지키지 않으면 막는다.")
    void signupPassword() {

        assertEquals(Set.of("password"), fields(signup("aaaaaaaaaa", 30, "160", "55")));
        assertEquals(Set.of("password"), fields(signup("abc12345", 30, "160", "55")));
        assertTrue(fields(signup("        ", 30, "160", "55")).contains("password"));

    }

    @Test
    @DisplayName("수정: 공백만 있는 닉네임과 범위를 벗어난 값을 막는다.")
    void updateInvalid() {

        assertEquals(Set.of("nickname"), fields(update("          ", null, null, null)));
        assertEquals(Set.of("age"), fields(update(null, 9999, null, null)));
        assertEquals(Set.of("height", "weight"), fields(update(null, null, "-1", "0")));

    }

    @Test
    @DisplayName("수정: 보내지 않은 값은 검사하지 않는다.")
    void updateEmptyIsValid() {

        assertTrue(fields(update(null, null, null, null)).isEmpty());
        assertTrue(fields(update("한결이", 31, "165", "53.5")).isEmpty());

    }

    @Test
    @DisplayName("재설정: 새 비밀번호에도 같은 규칙을 적용한다.")
    void resetPassword() {

        assertEquals(Set.of("newPassword"), fields(new ResetPasswordRequest("token", "aaaaaaaaaa")));
        assertTrue(fields(new ResetPasswordRequest("token", "password12!")).isEmpty());

    }

}
