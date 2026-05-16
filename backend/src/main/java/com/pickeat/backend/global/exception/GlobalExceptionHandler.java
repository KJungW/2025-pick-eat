package com.pickeat.backend.global.exception;

import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.global.exception.type.ExternalApiException;
import com.pickeat.backend.global.exception.type.InvalidRequestException;
import com.pickeat.backend.global.log.LogWriter;
import com.pickeat.backend.global.log.model.error.ClientErrorLog;
import com.pickeat.backend.global.log.model.error.ExternalErrorLog;
import com.pickeat.backend.global.log.model.error.ServerErrorLog;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException exception) {
        logInfoClientError(exception);
        return makeProblemDetail(exception.getErrorCode());
        //TODO: 내부 에러 분리하면 따로 500 처리 필요  (2026-05-16, 토, 20:49)
    }

    @ExceptionHandler(ExternalApiException.class)
    public ProblemDetail handleExternalApiException(ExternalApiException exception) {
        logExternalError(exception);
        return makeProblemDetail(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        InvalidRequestException invalidRequestException = new InvalidRequestException(
                ErrorCode.REQUEST_VALIDATION_FAILED);

        logInfoClientError(invalidRequestException);
        return makeProblemDetail(invalidRequestException.getErrorCode(), fieldErrors);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class
    })
    public ProblemDetail handleTypeMismatchException(Exception e) {
        logInfo(e, "BINDING_ERROR");

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(HttpStatus.BAD_REQUEST.name());
        problemDetail.setDetail("요청 파라미터 형식이 잘못되었습니다.");
        return problemDetail;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestPartException.class})
    public ProblemDetail handleInvalidRequestFormat(Exception e) {
        logInfo(e, "PARSING_ERROR");

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(HttpStatus.BAD_REQUEST.name());
        problemDetail.setDetail("요청 형식이 잘못되었습니다.");
        return problemDetail;
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            NoResourceFoundException.class,
    })
    public ProblemDetail handleWrongRequest(Exception e) {
        logInfo(e, "NO_RESOURCE_FOUND");

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(HttpStatus.BAD_REQUEST.name());
        problemDetail.setDetail("존재하지 않은 API에 대한 요청입니다. HTTP 메서드와 URL을 다시 확인해주세요.");
        return problemDetail;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleWrongMediaType(Exception e) {
        logInfo(e, "NOT_SUPPORTED_MEDIA");

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(HttpStatus.BAD_REQUEST.name());
        problemDetail.setDetail("허용하지 않는 미디어타입입니다. 요청 형식을 다시 확인해주세요.");
        return problemDetail;
    }

    @ExceptionHandler(MultipartException.class)
    public ProblemDetail handleInvalidMultiPartFormRequest(Exception e) {
        logInfo(e, "INVALID_MULTIPART");

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(HttpStatus.BAD_REQUEST.name());
        problemDetail.setDetail("잘못된 multipart/form-data 요청입니다. 요청 형식이나 업로드할 파일의 크기를 다시 확인해주세요.");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception e) {
        logServerError(e);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.name());
        problemDetail.setDetail("예상치 못한 오류가 발생했습니다.");
        return problemDetail;
    }

    private void logServerError(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        LogWriter.error(this.getClass(), ServerErrorLog.of(errorCode, exception));
    }

    private void logInfoClientError(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ClientErrorLog clientErrorLog = ClientErrorLog.of(errorCode, exception);

        HttpStatus status = errorCode.getStatus();
        if (status.isSameCodeAs(HttpStatusCode.valueOf(401))
                || status.isSameCodeAs(HttpStatusCode.valueOf(403))) {
            LogWriter.warn(this.getClass(), clientErrorLog);
        } else {
            LogWriter.info(this.getClass(), clientErrorLog);
        }
    }

    private void logExternalError(ExternalApiException exception) {
        ExternalErrorLog externalErrorLog = ExternalErrorLog.of(exception.getErrorCode(), exception);

        HttpStatus status = exception.getErrorCode().getStatus();
        if (status.is5xxServerError()) {
            LogWriter.error(this.getClass(), externalErrorLog);
        } else {
            LogWriter.warn(this.getClass(), externalErrorLog);
        }
    }

    private ProblemDetail makeProblemDetail(ErrorCode errorCode) {
        HttpStatus status = errorCode.getStatus();
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.name());
        problemDetail.setDetail(errorCode.getMessage());
        return problemDetail;
    }

    private ProblemDetail makeProblemDetail(ErrorCode errorCode, Map<String, String> fieldErrors) {
        HttpStatus status = errorCode.getStatus();
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.name());
        problemDetail.setDetail(errorCode.getMessage());
        problemDetail.setProperty("fieldErrors", fieldErrors);
        return problemDetail;
    }
}
