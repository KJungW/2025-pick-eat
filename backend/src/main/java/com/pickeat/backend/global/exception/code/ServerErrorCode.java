package com.pickeat.backend.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ServerErrorCode implements ErrorCode {

    // - Storage 관련 에러
    INVALID_STORAGE_KEY_ARGUMENT_COUNT(HttpStatus.INTERNAL_SERVER_ERROR, "Storage 키를 생성하기 위한 인자의 개수가 적절하지 않습니다."),
    INVALID_STORAGE_KEY_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "Storage 키를 생성하기 위한 인자 형식 적절하지 않습니다."),
    STORAGE_KEY_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Storage 키를 생성하는데 실패했습니다."),

    // - 기본적인 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ServerErrorCode(HttpStatus status, String message) {
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
