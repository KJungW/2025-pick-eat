package com.pickeat.backend.pickeat.application.publisher;

import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEvent;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PickeatEventHandler {

    private final ParticipantStorage participantStorage;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonParser jsonParser;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePickeatCompletionEvent(PickeatCompletionEventRequest request) {
        PickeatCompletionEvent event = new PickeatCompletionEvent(request.pickeatCode(), request.pickeatResult());
        String topicName = SseChannelTopic.PICKEAT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }
}
