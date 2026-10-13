package com.mealgram.common.exception;

// 외부 API 호출 실패 예외

public class ExternalApiException extends RuntimeException {

    private final String service;

    public ExternalApiException(String service, String reason) {
        super(service + " 호출 실패: " + reason);
        this.service = service;
    }

    public ExternalApiException(String service, String reason, Throwable cause) {
        super(service + " 호출 실패: " + reason, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }

}
