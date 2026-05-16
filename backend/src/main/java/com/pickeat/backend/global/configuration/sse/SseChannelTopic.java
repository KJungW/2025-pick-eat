package com.pickeat.backend.global.configuration.sse;

import lombok.Getter;

@Getter
public enum SseChannelTopic {

    PICKEAT_EVENT_TOPIC("pickeat-event-topic"),
    PARTICIPANT_EVENT_TOPIC("participant-event-topic"),
    RESTAURANT_EVENT_TOPIC("restaurant-event-topic");

    private final String value;

    SseChannelTopic(String value) {
        this.value = value;
    }
}
