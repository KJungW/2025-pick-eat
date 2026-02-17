package com.pickeat.backend.global.setting;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
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
    RESTAURANT_META("pickeat:%s:restaurants", 1),
    RESTAURANT_ALIVE("pickeat:%s:restaurants:alives", 1),
    RESTAURANT_LIKE_COUNT("pickeat:%s:likes:count", 1),
    RESTAURANT_LIKE_RECORD("pickeat:%s:restaurants:%s:likes", 2);

    public static final Duration PICKEAT_TTL = Duration.ofMinutes(30);

    private final String format;
    private final int argCount;

    public String generateKey(Object... args) {
        validateArgCount(args);
        return formatArgs(args);
    }

    private void validateArgCount(Object[] args) {
        if (args == null || args.length == 0 || args.length != this.argCount) {
            throw new BusinessException(ErrorCode.INVALID_STORAGE_KEY_ARGUMENT_COUNT);
        }
    }

    private String formatArgs(Object[] args) {
        try {
            return String.format(this.format, args);
        } catch (IllegalFormatConversionException e) {
            System.out.println("args[0] = " + args[0]);
            throw new BusinessException(ErrorCode.INVALID_STORAGE_KEY_FORMAT);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.STORAGE_KEY_CREATION_FAILED);
        }
    }
}
