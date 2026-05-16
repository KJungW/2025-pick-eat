package com.pickeat.backend.restaurant.application.publisher;

import com.pickeat.backend.global.configuration.sse.SseChannelTopic;
import com.pickeat.backend.global.configuration.sse.event.EventAction;
import com.pickeat.backend.global.configuration.sse.event.EventGroup;
import com.pickeat.backend.global.configuration.sse.event.EventMeta;
import com.pickeat.backend.global.configuration.sse.event.PickeatEvent;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantUpdateEventContent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantUpdateEventRequest;
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
    public void handleRestaurantUpdate(RestaurantUpdateEventRequest request) {
        String pickeatCode = request.pickeatCode();
        RestaurantStateDto state = getRestaurantStateWithSequence(pickeatCode);

        EventMeta eventMeta = new EventMeta(
                EventGroup.RESTAURANT,
                state.sequence(),
                EventAction.RESTAURANT_UPDATE_EVENT,
                pickeatCode);
        RestaurantUpdateEventContent content = RestaurantUpdateEventContent.of(state);
        PickeatEvent<RestaurantUpdateEventContent> event = PickeatEvent.of(eventMeta, content);

        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private RestaurantStateDto getRestaurantStateWithSequence(String pickeatCode) {
        return restaurantsStorage.getRestaurantStateWithSequence(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }
}
