package com.pickeat.backend.global.log.model.http;

import com.pickeat.backend.global.log.model.Log;
import com.pickeat.backend.global.log.model.LogType;
import java.util.Map;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public record ResponseLog(
        LogType logType,
        String uri,
        int status
) implements Log {

    public static ResponseLog of(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        return new ResponseLog(
                LogType.RESPONSE,
                request.getRequestURI(),
                response.getStatus()
        );
    }

    @Override
    public Map<String, Object> fields() {
        return Map.of(
                "logType", logType.name(),
                "uri", uri,
                "status", status
        );
    }

    @Override
    public String summary() {
        return String.format("[%s] %s %d", logType.name(), uri, status);
    }
}
