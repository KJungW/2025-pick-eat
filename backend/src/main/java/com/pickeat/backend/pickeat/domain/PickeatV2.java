package com.pickeat.backend.pickeat.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PickeatV2 {

    private final String code;
    private final String name;
    private final Long roomId;

    public static PickeatV2 createWithoutRoom(String name) {
        return new PickeatV2(UUID.randomUUID().toString(), name, null);
    }

    public static PickeatV2 createWithRoom(String name, Long roomId) {
        return new PickeatV2(UUID.randomUUID().toString(), name, roomId);
    }
}
