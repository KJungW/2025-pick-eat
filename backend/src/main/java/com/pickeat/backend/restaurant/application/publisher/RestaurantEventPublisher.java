package com.pickeat.backend.restaurant.application.publisher;

import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEvent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantLikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantEventPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonParser jsonParser;

    public void publishRestaurantExcludeEvent(RestaurantExcludeEvent event) {
        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    public void publishRestaurantLikeEvent(RestaurantLikeEvent event) {
        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }
}
