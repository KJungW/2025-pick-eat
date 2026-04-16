package com.pickeat.backend.restaurant.application.publisher;

import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEvent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantLikeEvent;
import java.util.Map;
import java.util.Set;
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

    public void publishRestaurantExcludeEvent(Set<String> aliveRestaurantIds) {
        String topicName = pickeatEventTopic.getTopic();
        RestaurantExcludeEvent event = new RestaurantExcludeEvent(aliveRestaurantIds);
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    public void publishRestaurantLikeEvent(Map<String, Integer> likeCountByRestaurant) {
        String topicName = pickeatEventTopic.getTopic();
        RestaurantLikeEvent event = new RestaurantLikeEvent(likeCountByRestaurant);
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }
}
