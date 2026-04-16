package com.pickeat.backend.participant.application.publisher;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.application.dto.ParticipantStateWithSequenceDto;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEvent;
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
        ParticipantStateWithSequenceDto state = getParticipantsStateInPickeat(pickeatCode);
        ParticipantUpdateEvent event = new ParticipantUpdateEvent(
                pickeatCode, state.completionState(), state.sequence());
        String topicName = SseChannelTopic.PARTICIPANT_EVENT_TOPIC.getValue();
        stringRedisTemplate.convertAndSend(topicName, jsonParser.toJson(event));
    }

    private ParticipantStateWithSequenceDto getParticipantsStateInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsStateWithSequence(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));
    }
}
