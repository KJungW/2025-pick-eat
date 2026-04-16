package com.pickeat.backend.global.sse;

public record EventMeta(
        EventGroup group,
        Long groupSequence,
        EventAction action,
        String pickeatCode
) {

}
