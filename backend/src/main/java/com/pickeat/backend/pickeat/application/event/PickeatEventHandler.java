package com.pickeat.backend.pickeat.application.event;

import com.pickeat.backend.global.configuration.sse.SseChannelTopic;
import com.pickeat.backend.global.configuration.sse.event.EventAction;
import com.pickeat.backend.global.configuration.sse.event.EventGroup;
import com.pickeat.backend.global.configuration.sse.event.EventMeta;
import com.pickeat.backend.global.configuration.sse.event.PickeatEvent;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEventContent;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PickeatEventHandler {

    private static final Long INVALID_SEQUENCE = -1L;

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePickeatCompletionEvent(PickeatCompletionEventRequest request) {
        PickeatEvent<PickeatCompletionEventContent> event = createEvent(request);
        String topicName = SseChannelTopic.PICKEAT_EVENT_TOPIC.getValue();
        redisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private PickeatEvent<PickeatCompletionEventContent> createEvent(PickeatCompletionEventRequest request) {
        EventMeta meta = new EventMeta(
                EventGroup.PICKEAT,
                INVALID_SEQUENCE,
                EventAction.PICKEAT_COMPLETION_EVENT,
                request.pickeatCode()
        );
        PickeatCompletionEventContent content = new PickeatCompletionEventContent(request.pickeatResult());
        return PickeatEvent.of(meta, content);
    }
}
