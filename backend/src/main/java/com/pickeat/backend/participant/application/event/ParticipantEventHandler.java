package com.pickeat.backend.participant.application.event;

import com.pickeat.backend.global.configuration.sse.SseChannelTopic;
import com.pickeat.backend.global.configuration.sse.event.EventAction;
import com.pickeat.backend.global.configuration.sse.event.EventGroup;
import com.pickeat.backend.global.configuration.sse.event.EventMeta;
import com.pickeat.backend.global.configuration.sse.event.PickeatEvent;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEventContent;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ParticipantEventHandler {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipantUpdate(ParticipantUpdateEventRequest request) {
        PickeatEvent<ParticipantUpdateEventContent> event = createEvent(request);
        String topicName = SseChannelTopic.PARTICIPANT_EVENT_TOPIC.getValue();
        redisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private PickeatEvent<ParticipantUpdateEventContent> createEvent(ParticipantUpdateEventRequest request) {
        EventMeta meta = new EventMeta(
                EventGroup.PARTICIPANT,
                request.sequence(),
                EventAction.PARTICIPANT_UPDATE_EVENT,
                request.pickeatCode()
        );
        ParticipantUpdateEventContent content = new ParticipantUpdateEventContent(request.completionState());
        return PickeatEvent.of(meta, content);
    }
}
