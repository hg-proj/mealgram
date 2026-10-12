package com.mealgram.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// 비밀번호 규칙 검증 어노테이션(8~20자, 영문과 숫자와 특수문자 3종류 이상은 8자부터, 2종류는 10자 이상)

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "비밀번호는 8~20자이고, 영문, 숫자, 특수문자 중 3종류 이상이면 8자 이상, 2종류면 10자 이상이어야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
