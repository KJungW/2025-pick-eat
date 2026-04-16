package com.pickeat.backend.global.cache;

import lombok.Getter;

@Getter
public enum CacheChannelTopic {

    TEMPLATE_TOPIC("template-topic");

    private final String value;

    CacheChannelTopic(String value) {
        this.value = value;
    }
}
