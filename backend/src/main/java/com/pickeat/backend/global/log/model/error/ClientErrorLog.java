package com.pickeat.backend.global.log.model.error;

import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.log.model.Log;
import com.pickeat.backend.global.log.model.LogType;
import java.util.HashMap;
import java.util.Map;

public record ClientErrorLog(
        LogType logType,
        ErrorCode errorCode,
        Throwable exception
) implements Log {

    public static ClientErrorLog of(
            ErrorCode errorCode,
            Throwable exception
    ) {
        return new ClientErrorLog(LogType.CLIENT_ERROR, errorCode, exception);
    }

    @Override
    public Map<String, Object> fields() {
        Map<String, Object> map = new HashMap<>();
        map.put("logType", logType.name());
        map.put("status", errorCode.getStatus());
        map.put("errorCode", errorCode.name());
        map.put("errorType", exception.getClass().getName());
        map.put("errorMessage", exception.getMessage());
        map.put("stackTrace", getStackTraceAsString(exception));
        return map;
    }

    @Override
    public String summary() {
        return String.format(
                "[%s] %d %s occurred",
                logType.name(),
                errorCode.getStatus().value(),
                errorCode.name()
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
