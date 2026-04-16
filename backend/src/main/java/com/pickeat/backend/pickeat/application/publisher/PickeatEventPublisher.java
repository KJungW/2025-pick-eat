package com.pickeat.backend.pickeat.application.publisher;

import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickeatEventPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonParser jsonParser;

    public void publishPickeatCompletionEvent(PickeatCompletionEvent event) {
        String topicName = SseChannelTopic.PICKEAT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }
}
