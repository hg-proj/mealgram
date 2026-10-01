package com.mealgram.common.response;

import com.mealgram.common.exception.ErrorCode;

// 실패 응답 포맷

public record ErrorResponse(

    boolean success,

    ErrorDetail error

) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(false, new ErrorDetail(errorCode.name(), errorCode.getMessage()));
    }

    public record ErrorDetail(String code, String message) {}

}
