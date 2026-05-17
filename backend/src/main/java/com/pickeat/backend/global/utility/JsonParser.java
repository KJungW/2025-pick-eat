package com.pickeat.backend.global.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickeat.backend.global.exception.code.ServerErrorCode;
import com.pickeat.backend.global.exception.type.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonParser {

    private final ObjectMapper objectMapper;

    public <T> String toJson(T data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new ServerException(ServerErrorCode.INTERNAL_SERVER_ERROR, exception);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException exception) {
            throw new ServerException(ServerErrorCode.INTERNAL_SERVER_ERROR, exception);
        }
    }
}
