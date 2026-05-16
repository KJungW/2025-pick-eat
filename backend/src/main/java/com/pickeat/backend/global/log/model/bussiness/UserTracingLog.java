package com.pickeat.backend.global.log.model.bussiness;

import com.pickeat.backend.global.log.model.Log;
import com.pickeat.backend.global.log.model.LogType;
import java.util.Map;

public record UserTracingLog(
        LogType logType,
        Long userId,
        String action
) implements Log {

    public static UserTracingLog of(Long userId, String action) {
        return new UserTracingLog(LogType.USER_TRACE, userId, action);
    }

    @Override
    public Map<String, Object> fields() {
        return Map.of(
                "logType", logType.name(),
                "userId", userId,
                "action", action
        );
    }

    @Override
    public String summary() {
        return String.format("[%s] User %d executed %s", logType.name(), userId, action);
    }
}
