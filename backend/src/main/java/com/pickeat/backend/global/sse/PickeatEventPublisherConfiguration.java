package com.pickeat.backend.global.sse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

@Configuration
public class PickeatEventPublisherConfiguration {

    private static final String PICKEAT_EVENT_TOPIC = "pickeat-event-topic";

    @Bean
    public ChannelTopic pickeatEventTopic() {
        return new ChannelTopic(PICKEAT_EVENT_TOPIC);
    }
}
