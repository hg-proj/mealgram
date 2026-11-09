package com.mealgram.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.meal.dto.MealRequest;
import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;
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

    private LocalDate yearsAgo(int years) {

        return LocalDate.now().minusYears(years);

    }

    private SignupRequest signup(String password, LocalDate birthDate, String height, String weight) {

        return new SignupRequest("한결", "hangyeol01", password, birthDate, null,
                height == null ? null : new BigDecimal(height), weight == null ? null : new BigDecimal(weight),
                null, "test@example.com");

    }

    private MemberUpdateRequest update(String nickname, LocalDate birthDate, String height, String weight) {

        return new MemberUpdateRequest(nickname, birthDate, null, height == null ? null : new BigDecimal(height),
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

        assertTrue(fields(signup("password12!", yearsAgo(30), "160", "55")).isEmpty());
        assertTrue(fields(signup("password12!", null, null, null)).isEmpty());

    }

    @Test
    @DisplayName("가입: 생년월일, 키, 몸무게가 범위를 벗어나면 막는다.")
    void signupOutOfRange() {

        assertEquals(Set.of("birthDate"), fields(signup("password12!", yearsAgo(2), "160", "55")));
        assertEquals(Set.of("birthDate"), fields(signup("password12!", yearsAgo(121), "160", "55")));
        assertEquals(Set.of("birthDate"), fields(signup("password12!", LocalDate.now().plusDays(1), "160", "55")));
        assertEquals(Set.of("height"), fields(signup("password12!", yearsAgo(30), "49.9", "55")));
        assertEquals(Set.of("height"), fields(signup("password12!", yearsAgo(30), "251", "55")));
        assertEquals(Set.of("weight"), fields(signup("password12!", yearsAgo(30), "160", "9.9")));
        assertEquals(Set.of("weight"), fields(signup("password12!", yearsAgo(30), "160", "300.1")));

    }

    @Test
    @DisplayName("가입: 3세, 120세와 경계값은 허용한다.")
    void signupBoundaries() {

        assertTrue(fields(signup("password12!", yearsAgo(3), "50", "10")).isEmpty());
        assertTrue(fields(signup("password12!", yearsAgo(120), "250", "300")).isEmpty());

    }

    @Test
    @DisplayName("가입: 생일이 하루 모자라 만 3세가 아니면 막고 하루 지나 만 120세면 허용한다.")
    void signupBirthDateByDay() {

        assertEquals(Set.of("birthDate"), fields(signup("password12!", yearsAgo(3).plusDays(1), "160", "55")));
        assertTrue(fields(signup("password12!", yearsAgo(121).plusDays(1), "160", "55")).isEmpty());

    }

    @Test
    @DisplayName("가입: 비밀번호 규칙을 지키지 않으면 막는다.")
    void signupPassword() {

        assertEquals(Set.of("password"), fields(signup("aaaaaaaaaa", yearsAgo(30), "160", "55")));
        assertEquals(Set.of("password"), fields(signup("abc12345", yearsAgo(30), "160", "55")));
        assertTrue(fields(signup("        ", yearsAgo(30), "160", "55")).contains("password"));

    }

    @Test
    @DisplayName("수정: 공백만 있는 닉네임과 범위를 벗어난 값을 막는다.")
    void updateInvalid() {

        assertEquals(Set.of("nickname"), fields(update("          ", null, null, null)));
        assertEquals(Set.of("birthDate"), fields(update(null, LocalDate.of(1800, 1, 1), null, null)));
        assertEquals(Set.of("height", "weight"), fields(update(null, null, "-1", "0")));

    }

    @Test
    @DisplayName("수정: 보내지 않은 값은 검사하지 않는다.")
    void updateEmptyIsValid() {

        assertTrue(fields(update(null, null, null, null)).isEmpty());
        assertTrue(fields(update("한결이", yearsAgo(31), "165", "53.5")).isEmpty());

    }

    @Test
    @DisplayName("재설정: 새 비밀번호에도 같은 규칙을 적용한다.")
    void resetPassword() {

        assertEquals(Set.of("newPassword"), fields(new ResetPasswordRequest("token", "aaaaaaaaaa")));
        assertTrue(fields(new ResetPasswordRequest("token", "password12!")).isEmpty());

    }

    @Test
    @DisplayName("식단 추천: 목표는 다이어트, 벌크업, 유지 중 하나이거나 비어 있어야 한다.")
    void mealGoal() {

        assertTrue(fields(new MealRequest(null, null, null, "다이어트", null, null, null, null, null, null)).isEmpty());
        assertTrue(fields(new MealRequest(null, null, null, "벌크업", "한식", null, null, null, null, null)).isEmpty());
        assertTrue(fields(new MealRequest(null, null, null, "유지", null, null, null, null, null, null)).isEmpty());
        assertTrue(fields(new MealRequest(null, null, null, null, null, null, null, null, null, null)).isEmpty());
        assertEquals(Set.of("goal"), fields(new MealRequest(null, null, null, "아무거나", null, null, null, null, null, null)));
        assertEquals(Set.of("goal"), fields(new MealRequest(null, null, null, "", null, null, null, null, null, null)));

    }

    @Test
    @DisplayName("식단 추천: 비회원 신체 정보는 가입과 같은 범위 안이어야 한다.")
    void mealBodyInfo() {

        assertTrue(fields(new MealRequest(null, null, null, null, null, yearsAgo(30), Gender.MALE, new BigDecimal("175"),
                new BigDecimal("70"), ActivityLevel.MODERATE)).isEmpty());
        assertEquals(Set.of("birthDate"),
                fields(new MealRequest(null, null, null, null, null, yearsAgo(2), null, null, null, null)));
        assertEquals(Set.of("birthDate"),
                fields(new MealRequest(null, null, null, null, null, yearsAgo(121), null, null, null, null)));
        assertEquals(Set.of("height"),
                fields(new MealRequest(null, null, null, null, null, null, null, new BigDecimal("49"), null, null)));
        assertEquals(Set.of("weight"),
                fields(new MealRequest(null, null, null, null, null, null, null, null, new BigDecimal("301"), null)));

    }

}
