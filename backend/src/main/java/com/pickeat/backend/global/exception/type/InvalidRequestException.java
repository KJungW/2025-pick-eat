package com.pickeat.backend.global.exception.type;

import com.pickeat.backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public InvalidRequestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
