package com.pickeat.backend.global.exception;

import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.global.exception.type.ExternalException;
import com.pickeat.backend.global.exception.type.ServerException;
import com.pickeat.backend.global.log.LogWriter;
import com.pickeat.backend.global.log.model.error.ClientErrorLog;
import com.pickeat.backend.global.log.model.error.ExternalErrorLog;
import com.pickeat.backend.global.log.model.error.ServerErrorLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Component
public class ExceptionLogWriter {

    public void logInfoClientError(ClientException exception) {
        ClientErrorLog clientErrorLog = ClientErrorLog.of(exception);

        HttpStatus status = exception.getErrorCode().getStatus();
        if (status.isSameCodeAs(HttpStatusCode.valueOf(401))
                || status.isSameCodeAs(HttpStatusCode.valueOf(403))) {
            LogWriter.warn(this.getClass(), clientErrorLog);
        } else {
            LogWriter.info(this.getClass(), clientErrorLog);
        }
    }

    public void logExternalError(ExternalException exception) {
        ExternalErrorLog externalErrorLog = ExternalErrorLog.of(exception);

        HttpStatus status = exception.getErrorCode().getStatus();
        if (status.is5xxServerError()) {
            LogWriter.error(this.getClass(), externalErrorLog);
        } else {
            LogWriter.warn(this.getClass(), externalErrorLog);
        }
    }

    public void logServerError(ServerException exception) {
        LogWriter.error(this.getClass(), ServerErrorLog.of(exception));
    }
}
