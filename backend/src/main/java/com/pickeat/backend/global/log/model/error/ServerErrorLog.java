package com.pickeat.backend.global.log.model.error;

import com.pickeat.backend.global.exception.code.ServerErrorCode;
import com.pickeat.backend.global.exception.type.ServerException;
import com.pickeat.backend.global.log.model.Log;
import com.pickeat.backend.global.log.model.LogType;
import java.util.HashMap;
import java.util.Map;

public record ServerErrorLog(
        LogType logType,
        ServerErrorCode errorCode,
        Throwable exception
) implements Log {

    public static ServerErrorLog of(ServerException exception) {
        ServerErrorCode errorCode = (ServerErrorCode) exception.getErrorCode();
        if (exception.getCause() == null) {
            return new ServerErrorLog(LogType.SERVER_ERROR, errorCode, exception);
        }
        return new ServerErrorLog(LogType.SERVER_ERROR, errorCode, exception.getCause());
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
