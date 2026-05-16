package com.pickeat.backend.global.configuration.sse.event;

public record EventMeta(
        EventGroup group,
        Long groupSequence,
        EventAction action,
        String pickeatCode,
        long publishedAt
) {

    public EventMeta(
            EventGroup group,
            Long groupSequence,
            EventAction action,
            String pickeatCode
    ) {
        this(group, groupSequence, action, pickeatCode, System.currentTimeMillis());
    }
}
