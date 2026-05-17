package com.pickeat.backend.global.log.model.bussiness;

import com.pickeat.backend.global.log.model.Log;
import com.pickeat.backend.global.log.model.LogType;
import java.util.Map;

public record DebugLog(
        LogType logType,
        String message
) implements Log {

    public static DebugLog of(String message) {
        return new DebugLog(LogType.DEBUG, message);
    }

    @Override
    public Map<String, Object> fields() {
        return Map.of(
                "logType", logType.name(),
                "message", message
        );
    }

    @Override
    public String summary() {
        return String.format("[%s] %s", logType.name(), message);
    }
}
