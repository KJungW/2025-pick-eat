package com.pickeat.backend.global.exception.code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getName();

    HttpStatus getStatus();

    String getMessage();
}
