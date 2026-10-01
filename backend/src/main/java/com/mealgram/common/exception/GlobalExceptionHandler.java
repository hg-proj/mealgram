package com.mealgram.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mealgram.common.response.ErrorResponse;

// 터진 예외를 잡아 응답 포맷으로 변환

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)

    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                    .status(errorCode.getHttpStatus())
                    .body(ErrorResponse.of(errorCode));
    }
}
