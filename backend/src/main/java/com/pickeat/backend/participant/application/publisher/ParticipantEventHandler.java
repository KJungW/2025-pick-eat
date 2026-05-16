package com.pickeat.backend.participant.application.publisher;

import com.pickeat.backend.global.configuration.sse.SseChannelTopic;
import com.pickeat.backend.global.configuration.sse.event.EventAction;
import com.pickeat.backend.global.configuration.sse.event.EventGroup;
import com.pickeat.backend.global.configuration.sse.event.EventMeta;
import com.pickeat.backend.global.configuration.sse.event.PickeatEvent;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEventContent;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEventRequest;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ParticipantEventHandler {

    private final ParticipantStorage participantStorage;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonParser jsonParser;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipantUpdate(ParticipantUpdateEventRequest request) {
        String pickeatCode = request.pickeatCode();
        ParticipantStateDto state = getParticipantsStateInPickeat(pickeatCode);

        EventMeta eventMeta = new EventMeta(
                EventGroup.PARTICIPANT,
                state.sequence(),
                EventAction.PARTICIPANT_UPDATE_EVENT,
                pickeatCode);
        ParticipantUpdateEventContent content = new ParticipantUpdateEventContent(state.completionState());
        PickeatEvent<ParticipantUpdateEventContent> event = PickeatEvent.of(eventMeta, content);

        String topicName = SseChannelTopic.PARTICIPANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private ParticipantStateDto getParticipantsStateInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsStateWithSequence(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));
    }
}
