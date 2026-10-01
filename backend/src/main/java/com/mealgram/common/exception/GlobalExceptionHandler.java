package com.mealgram.common.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mealgram.common.response.ErrorResponse;
import com.mealgram.common.response.ErrorResponse.FieldErrorDetail;

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

    @ExceptionHandler(MethodArgumentNotValidException.class) 
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        
        List<FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                                .toList();

        return ResponseEntity
                        .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.VALIDATION_FAILED, fieldErrors));
    }

    @ExceptionHandler (HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity
                        .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST));
    }  

}
