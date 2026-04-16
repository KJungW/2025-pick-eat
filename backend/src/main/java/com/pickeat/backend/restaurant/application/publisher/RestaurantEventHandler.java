package com.pickeat.backend.restaurant.application.publisher;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.sse.EventAction;
import com.pickeat.backend.global.sse.EventGroup;
import com.pickeat.backend.global.sse.EventMeta;
import com.pickeat.backend.global.sse.PickeatEvent;
import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEventContent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEventRequest;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantLikeEventContent;
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
        RestaurantStateDto state = getRestaurantStateWithSequence(pickeatCode);

        EventMeta eventMeta = new EventMeta(
                EventGroup.RESTAURANT,
                state.sequence(),
                EventAction.RESTAURANT_EXCLUDE_EVENT,
                pickeatCode);
        RestaurantExcludeEventContent content = new RestaurantExcludeEventContent(state.aliveRestaurantCode());
        PickeatEvent<RestaurantExcludeEventContent> event = PickeatEvent.of(eventMeta, content);

        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRestaurantLike(RestaurantLikeEventRequest request) {
        String pickeatCode = request.pickeatCode();
        RestaurantStateDto state = getRestaurantStateWithSequence(pickeatCode);

        EventMeta eventMeta = new EventMeta(
                EventGroup.RESTAURANT,
                state.sequence(),
                EventAction.RESTAURANT_LIKE_EVENT,
                pickeatCode);
        RestaurantLikeEventContent content = new RestaurantLikeEventContent(state.likeCountByRestaurant());
        PickeatEvent<RestaurantLikeEventContent> event = PickeatEvent.of(eventMeta, content);

        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private RestaurantStateDto getRestaurantStateWithSequence(String pickeatCode) {
        return restaurantsStorage.getRestaurantStateWithSequence(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }
}
