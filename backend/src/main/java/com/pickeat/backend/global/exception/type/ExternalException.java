package com.pickeat.backend.global.exception.type;

import com.pickeat.backend.global.exception.code.ExternalErrorCode;
import com.pickeat.backend.global.exception.code.ServerErrorCode;
import lombok.Getter;

@Getter
public class ExternalException extends BaseException {

    private final String platformName;

    public ExternalException(ExternalErrorCode errorCode, String platformName) {
        super(errorCode);
        this.platformName = platformName;
    }

    public ExternalException(ExternalErrorCode errorCode, String platformName, String message) {
        super(errorCode, message);
        this.platformName = platformName;
    }

    public ExternalException(ExternalErrorCode errorCode, String platformName, Throwable cause) {
        super(errorCode, cause);
        this.platformName = platformName;
    }

    public ExternalException(ServerErrorCode errorCode, String platformName, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.platformName = platformName;
    }
}
