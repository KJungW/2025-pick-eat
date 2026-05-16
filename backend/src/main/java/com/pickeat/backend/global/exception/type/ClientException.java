package com.pickeat.backend.global.exception.type;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import lombok.Getter;

@Getter
public class ClientException extends BaseException {

    public ClientException(ClientErrorCode errorCode) {
        super(errorCode);
    }

    public ClientException(ClientErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ClientException(ClientErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ClientException(ClientErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
