package com.pickeat.backend.global.sse;

import lombok.Getter;

@Getter
public class PickeatEvent<T> {

    private final EventMeta meta;
    private final T content;

    private PickeatEvent(EventMeta meta, T content) {
        this.meta = meta;
        this.content = content;
    }

    public static <T> PickeatEvent<T> of(EventMeta meta, T content) {
        return new PickeatEvent<>(meta, content);
    }
}
