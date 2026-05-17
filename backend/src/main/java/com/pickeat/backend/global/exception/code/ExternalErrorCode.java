package com.pickeat.backend.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ExternalErrorCode implements ErrorCode {

    EXTERNAL_API_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출에서 예외 응답 발생"),
    EXTERNAL_API_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출에서 타임아웃 발생"),
    RATE_LIMIT_WAIT_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "요청 제한을 위한 토큰 대기에서 타임아웃 발생"),
    THREAD_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API를 호출하는 스레드에서 타임아웃 발생");

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
