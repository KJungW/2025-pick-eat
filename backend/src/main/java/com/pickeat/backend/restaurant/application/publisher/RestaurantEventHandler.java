package com.pickeat.backend.restaurant.application.publisher;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEvent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEventRequest;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantLikeEvent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantLikeEventRequest;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RestaurantEventHandler {

    private final RestaurantsStorage restaurantsStorage;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonParser jsonParser;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRestaurantExclude(RestaurantExcludeEventRequest request) {
        String pickeatCode = request.pickeatCode();
        RestaurantStateDto pickeatState = getRestaurantStateWithSequence(pickeatCode);
        RestaurantExcludeEvent event = new RestaurantExcludeEvent(pickeatCode, pickeatState.aliveRestaurantCode());
        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRestaurantLike(RestaurantLikeEventRequest request) {
        String pickeatCode = request.pickeatCode();
        RestaurantStateDto pickeatState = getRestaurantStateWithSequence(pickeatCode);
        RestaurantLikeEvent event = new RestaurantLikeEvent(pickeatCode, pickeatState.likeCountByRestaurant());
        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private RestaurantStateDto getRestaurantStateWithSequence(String pickeatCode) {
        return restaurantsStorage.getRestaurantStateWithSequence(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }
}
