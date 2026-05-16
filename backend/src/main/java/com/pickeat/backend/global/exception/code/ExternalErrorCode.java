package com.pickeat.backend.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ExternalErrorCode implements ErrorCode {

    EXTERNAL_API_FAIL_5XX(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 연동에 실패했습니다.(5XX 응답 발생)"),
    EXTERNAL_API_FAIL_4XX(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 연결에 실패했습니다.(4XX 응답 발생)");

    private final HttpStatus status;
    private final String message;

    ExternalErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
