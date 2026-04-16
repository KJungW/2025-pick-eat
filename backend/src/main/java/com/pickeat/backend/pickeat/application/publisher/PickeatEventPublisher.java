package com.pickeat.backend.pickeat.application.publisher;

import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickeatEventPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelTopic pickeatEventTopic;
    private final JsonParser jsonParser;

    public void publishPickeatCompletionEvent(PickeatCompletionEvent event) {
        String topicName = pickeatEventTopic.getTopic();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }
}
