package com.mealgram.common.exception;

import org.springframework.http.HttpStatus;

// 예외 코드, HTTP 상태, 메시지 모음

public enum ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인해 주세요."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "재료를 찾을 수 없습니다."),
    DUPLICATE_MY_INGREDIENT(HttpStatus.CONFLICT, "이미 등록한 재료입니다."),
    MY_INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "등록한 재료를 찾을 수 없습니다."),
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다."),
    RECIPE_CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "조건에 맞는 레시피가 없습니다."),
    MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 결과를 찾을 수 없습니다."),
    DUPLICATE_SAVED_MEAL(HttpStatus.CONFLICT, "이미 저장한 식단입니다."),
    SAVED_MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "저장한 식단을 찾을 수 없습니다."),
    MEAL_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "식단 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

}
