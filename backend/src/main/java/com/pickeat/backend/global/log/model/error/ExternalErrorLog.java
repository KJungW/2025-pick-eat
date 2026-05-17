package com.pickeat.backend.global.log.model.error;

import com.pickeat.backend.global.exception.code.ExternalErrorCode;
import com.pickeat.backend.global.exception.type.ExternalException;
import com.pickeat.backend.global.log.model.Log;
import com.pickeat.backend.global.log.model.LogType;
import java.util.HashMap;
import java.util.Map;

public record ExternalErrorLog(
        LogType logType,
        ExternalErrorCode errorCode,
        Throwable exception,
        String platformName
) implements Log {

    public static ExternalErrorLog of(ExternalException exception) {
        ExternalErrorCode errorCode = (ExternalErrorCode) exception.getErrorCode();
        if (exception.getCause() == null) {
            return new ExternalErrorLog(
                    LogType.EXTERNAL_ERROR,
                    errorCode,
                    exception,
                    exception.getPlatformName()
            );
        }
        return new ExternalErrorLog(
                LogType.EXTERNAL_ERROR,
                errorCode,
                exception.getCause(),
                exception.getPlatformName()
        );
    }

    @Override
    public Map<String, Object> fields() {
        Map<String, Object> map = new HashMap<>();
        map.put("logType", logType.name());
        map.put("status", errorCode.getStatus());
        map.put("errorCode", errorCode.getName());
        map.put("errorType", exception.getClass().getName());
        map.put("errorMessage", exception.getMessage());
        map.put("stackTrace", getStackTraceAsString(exception));
        map.put("platformName", platformName);
        return map;
    }

    @Override
    public String summary() {
        return String.format(
                "[%s] %d %s occurred",
                logType.name(),
                errorCode.getStatus().value(),
                errorCode.getName()
        );
    }

    private static String getStackTraceAsString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = ex.getStackTrace();
        int limit = Math.min(stackTrace.length, 5);
        for (int i = 0; i < limit; i++) {
            sb.append(stackTrace[i]).append("\n");
        }
        return sb.toString();
    }
}
