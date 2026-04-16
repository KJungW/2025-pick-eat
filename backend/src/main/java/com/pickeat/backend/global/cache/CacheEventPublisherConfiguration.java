package com.pickeat.backend.global.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.listener.ChannelTopic;

public class CacheEventPublisherConfiguration {

    @Bean
    public ChannelTopic pickeatEventTopic() {
        return new ChannelTopic(CacheChannelTopic.PICKEAT_EVENT_TOPIC.getValue());
    }
}
