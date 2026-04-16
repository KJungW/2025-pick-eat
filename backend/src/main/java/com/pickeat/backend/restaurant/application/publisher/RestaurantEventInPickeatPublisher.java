package com.pickeat.backend.restaurant.application.publisher;

import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEvent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantLikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantEventInPickeatPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelTopic pickeatEventTopic;
    private final JsonParser jsonParser;

    public void publishRestaurantExcludeEvent(RestaurantExcludeEvent event) {
        String topicName = pickeatEventTopic.getTopic();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    public void publishRestaurantLikeEvent(RestaurantLikeEvent event) {
        String topicName = pickeatEventTopic.getTopic();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }
}
