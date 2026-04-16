package com.pickeat.backend.participant.application.publisher;

import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantEventPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonParser jsonParser;

    public void publishParticipantUpdateEvent(ParticipantUpdateEvent event) {
        String topicName = SseChannelTopic.PARTICIPANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }
}
