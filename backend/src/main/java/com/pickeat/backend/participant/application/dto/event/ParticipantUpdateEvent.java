package com.pickeat.backend.participant.application.dto.event;

import java.util.Map;
import lombok.Getter;

@Getter
public class ParticipantUpdateEvent {

    private final ParticipantEventType type;
    private final String pickeatCode;
    private final Map<String, Boolean> completion;

    public ParticipantUpdateEvent(String pickeatCode, Map<String, Boolean> completion) {
        this.type = ParticipantEventType.PARTICIPANT_UPDATE_EVENT;
        this.pickeatCode = pickeatCode;
        this.completion = completion;
    }
}
