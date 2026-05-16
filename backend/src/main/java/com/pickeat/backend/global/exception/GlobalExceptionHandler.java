package com.pickeat.backend.global.exception;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.code.ServerErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.global.exception.type.ExternalException;
import com.pickeat.backend.global.exception.type.ServerException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
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

    private final ProblemDetailFactory problemDetailFactory;
    private final ExceptionLogWriter logWriter;

    @ExceptionHandler(ClientException.class)
    public ProblemDetail handleClientException(ClientException exception) {
        logWriter.logInfoClientError(exception);
        return problemDetailFactory.make(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleRequestValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ClientException clientException = new ClientException(ClientErrorCode.REQUEST_VALIDATION_FAILED, exception);
        logWriter.logInfoClientError(clientException);
        return problemDetailFactory.make(clientException, fieldErrors);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class,
            HttpMessageNotReadableException.class
    })
    public ProblemDetail handleRequestPayloadException(Exception exception) {
        ClientException clientException = new ClientException(ClientErrorCode.REQUEST_VALIDATION_FAILED, exception);
        logWriter.logInfoClientError(clientException);
        return problemDetailFactory.make(clientException);
    }

    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            NoResourceFoundException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ProblemDetail handleHttpProtocolException(Exception exception) {
        ClientException clientException = new ClientException(ClientErrorCode.INVALID_HTTP_PROTOCOL, exception);
        logWriter.logInfoClientError(clientException);
        return problemDetailFactory.make(clientException);
    }

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MultipartException.class
    })
    public ProblemDetail handleMultipartUploadException(Exception exception) {
        ClientException clientException = new ClientException(ClientErrorCode.MULTIPART_UPLOAD_FAILED, exception);
        logWriter.logInfoClientError(clientException);
        return problemDetailFactory.make(clientException);
    }

    @ExceptionHandler(ExternalException.class)
    public ProblemDetail handleExternalApiException(ExternalException exception) {
        logWriter.logExternalError(exception);
        return problemDetailFactory.makeInternalServerError();
    }

    @ExceptionHandler(ServerException.class)
    public ProblemDetail handleServerException(ServerException exception) {
        logWriter.logServerError(exception);
        return problemDetailFactory.makeInternalServerError();
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception exception) {
        ServerException serverException = new ServerException(ServerErrorCode.INTERNAL_SERVER_ERROR, exception);
        logWriter.logServerError(serverException);
        return problemDetailFactory.makeInternalServerError();
    }
}
