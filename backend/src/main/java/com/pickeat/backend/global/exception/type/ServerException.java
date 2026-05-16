package com.pickeat.backend.global.exception.type;

import com.pickeat.backend.global.exception.code.ServerErrorCode;

public class ServerException extends BaseException {

    public ServerException(ServerErrorCode errorCode) {
        super(errorCode);
    }

    public ServerException(ServerErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ServerException(ServerErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ServerException(ServerErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
