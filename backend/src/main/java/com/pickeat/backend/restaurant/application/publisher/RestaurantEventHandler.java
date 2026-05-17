package com.pickeat.backend.restaurant.application.publisher;

import com.pickeat.backend.global.configuration.sse.SseChannelTopic;
import com.pickeat.backend.global.configuration.sse.event.EventAction;
import com.pickeat.backend.global.configuration.sse.event.EventGroup;
import com.pickeat.backend.global.configuration.sse.event.EventMeta;
import com.pickeat.backend.global.configuration.sse.event.PickeatEvent;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantUpdateEventContent;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantUpdateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RestaurantEventHandler {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRestaurantUpdate(RestaurantUpdateEventRequest request) {
        PickeatEvent<RestaurantUpdateEventContent> event = createEvent(request);
        String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
        redisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private PickeatEvent<RestaurantUpdateEventContent> createEvent(RestaurantUpdateEventRequest request) {
        RestaurantStateDto state = request.state();

        EventMeta meta = new EventMeta(
                EventGroup.RESTAURANT,
                state.sequence(),
                EventAction.RESTAURANT_UPDATE_EVENT,
                request.pickeatCode()
        );
        RestaurantUpdateEventContent content = RestaurantUpdateEventContent.of(state);
        return PickeatEvent.of(meta, content);
    }
}
