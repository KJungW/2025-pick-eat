package com.pickeat.backend.global.exception;

import com.pickeat.backend.global.exception.code.ErrorCode;
import com.pickeat.backend.global.exception.type.BaseException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailFactory {

    private static final String DEFAULT_SERVER_EXCEPTION_MESSAGE = "내부 서버 오류가 발생했습니다.";

    public ProblemDetail make(BaseException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = errorCode.getStatus();
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.name());
        problemDetail.setDetail(errorCode.getMessage());
        return problemDetail;
    }

    public ProblemDetail make(BaseException exception, Map<String, String> fieldErrors) {
        ErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = errorCode.getStatus();
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.name());
        problemDetail.setDetail(errorCode.getMessage());
        problemDetail.setProperty("fieldErrors", fieldErrors);
        return problemDetail;
    }

    public ProblemDetail makeInternalServerError() {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.name());
        problemDetail.setDetail(DEFAULT_SERVER_EXCEPTION_MESSAGE);
        return problemDetail;
    }
}
