package com.mealgram.common.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mealgram.common.exception.ErrorCode;

// 실패 응답 포맷

public record ErrorResponse(

    boolean success,

    ErrorDetail error

) {

    public static ErrorResponse of(ErrorCode errorCode) {

        return new ErrorResponse(false, new ErrorDetail(errorCode.name(), errorCode.getMessage(), null));

    }

    public static ErrorResponse of(ErrorCode errorCode, 
        List<FieldErrorDetail> fieldErrors) {

            return new ErrorResponse(false, new ErrorDetail(errorCode.name(), errorCode.getMessage(), fieldErrors));

        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ErrorDetail(String code, String message, List<FieldErrorDetail> fieldErrors) {}
        
        public record FieldErrorDetail(String field, String message){}

}
