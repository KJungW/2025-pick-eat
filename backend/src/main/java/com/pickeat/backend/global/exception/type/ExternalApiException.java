package com.pickeat.backend.global.exception.type;

import com.pickeat.backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String platformName;

    public ExternalApiException(ErrorCode errorCode, String platformName) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.platformName = platformName;
    }
}
