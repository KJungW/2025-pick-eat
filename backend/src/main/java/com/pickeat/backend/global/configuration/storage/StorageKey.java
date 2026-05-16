package com.pickeat.backend.global.configuration.storage;

import com.pickeat.backend.global.exception.code.ServerErrorCode;
import com.pickeat.backend.global.exception.type.ServerException;
import java.time.Duration;
import java.util.IllegalFormatConversionException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StorageKey {

    PICKEAT("pickeat:%s", 1),

    PARTICIPANT("pickeat:%s:participant", 1),
    PARTICIPANT_COMPLETION("pickeat:%s:participant:completion", 1),
    PARTICIPANT_SEQUENCE("pickeat:%s:participant:state:sequence", 1),

    RESTAURANT_META("pickeat:%s:restaurant", 1),
    RESTAURANT_ALIVE("pickeat:%s:restaurant:alive", 1),
    RESTAURANT_LIKE_COUNT("pickeat:%s:restaurant:like:count", 1),
    RESTAURANT_LIKE_RECORD("pickeat:%s:restaurant:%s:like:record", 2),
    RESTAURANT_SEQUENCE("pickeat:%s:restaurant:state:sequence", 1);

    public static final Duration PICKEAT_TTL = Duration.ofMinutes(30);

    private final String format;
    private final int argCount;

    public String generateKey(Object... args) {
        validateArgCount(args);
        return formatArgs(args);
    }

    private void validateArgCount(Object[] args) {
        if (args == null || args.length == 0 || args.length != this.argCount) {
            throw new ServerException(ServerErrorCode.INVALID_STORAGE_KEY_ARGUMENT_COUNT);
        }
    }

    private String formatArgs(Object[] args) {
        try {
            return String.format(this.format, args);
        } catch (IllegalFormatConversionException e) {
            System.out.println("args[0] = " + args[0]);
            throw new ServerException(ServerErrorCode.INVALID_STORAGE_KEY_FORMAT);
        } catch (Exception e) {
            throw new ServerException(ServerErrorCode.STORAGE_KEY_CREATION_FAILED);
        }
    }
}
