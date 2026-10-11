package com.mealgram.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mealgram.common.response.ErrorResponse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, ErrorCode code) {

        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals(code.name(), response.getBody().error().code());
        assertEquals(code.getMessage(), response.getBody().error().message());

    }

    @Test
    @DisplayName("파라미터 형식이 틀리면 400 공통 오류 형식으로 응답한다.")
    void invalidParameterType() {

        MethodArgumentTypeMismatchException e = new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);

        assertError(handler.handleInvalidParameter(e), HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST);

    }

    @Test
    @DisplayName("필수 파라미터가 없으면 400 공통 오류 형식으로 응답한다.")
    void missingParameter() {

        MissingServletRequestParameterException e = new MissingServletRequestParameterException("keyword", "String");

        assertError(handler.handleInvalidParameter(e), HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST);

    }

    @Test
    @DisplayName("없는 경로는 404 공통 오류 형식으로 응답한다.")
    void noResource() {

        NoResourceFoundException e = new NoResourceFoundException(HttpMethod.GET, "nothing", "nothing");

        assertError(handler.handleNoResource(e), HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);

    }

    @Test
    @DisplayName("지원하지 않는 메서드는 405 공통 오류 형식으로 응답한다.")
    void methodNotSupported() {

        assertError(handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("PUT")),
                HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED);

    }

    @Test
    @DisplayName("지원하지 않는 Content-Type은 415 공통 오류 형식으로 응답한다.")
    void mediaTypeNotSupported() {

        assertError(handler.handleMediaTypeNotSupported(new HttpMediaTypeNotSupportedException("text/plain")),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE);

    }

    @Test
    @DisplayName("업무 예외는 지정한 상태와 코드로 응답한다.")
    void businessException() {

        assertError(handler.handleBusinessException(new BusinessException(ErrorCode.MEMBER_NOT_FOUND)),
                HttpStatus.NOT_FOUND, ErrorCode.MEMBER_NOT_FOUND);

    }

}
