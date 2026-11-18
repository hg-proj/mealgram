package com.mealgram.common.exception;

import java.util.List;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mealgram.common.response.ErrorResponse;
import com.mealgram.common.response.ErrorResponse.FieldErrorDetail;

import lombok.extern.slf4j.Slf4j;

// 터진 예외를 잡아 응답 포맷으로 변환

@Slf4j
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

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleInvalidParameter(Exception e) {

        return ResponseEntity
                        .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST));

    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {

        return ResponseEntity
                        .status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));

    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {

        return ResponseEntity
                        .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));

    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {

        return ResponseEntity
                        .status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE));

    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException e) {

        return ResponseEntity
                        .status(ErrorCode.EXTERNAL_API_FAILED.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.EXTERNAL_API_FAILED));

    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("데이터 제약 조건을 어겼습니다: {}", e.getClass().getSimpleName());
        return ResponseEntity
                        .status(ErrorCode.DATA_CONFLICT.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.DATA_CONFLICT));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ErrorResponse> handleStoreUnavailable(DataAccessResourceFailureException e) {
        log.error("저장소에 연결하지 못했습니다: {}", e.getClass().getSimpleName());
        return ResponseEntity
                        .status(ErrorCode.SERVICE_UNAVAILABLE.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.SERVICE_UNAVAILABLE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {

        log.error("예상하지 못한 오류가 발생했습니다.", e);

        return ResponseEntity
                        .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                        .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));

    }

}
