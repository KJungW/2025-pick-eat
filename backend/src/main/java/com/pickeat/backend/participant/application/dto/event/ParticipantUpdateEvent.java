package com.pickeat.backend.participant.application.dto.event;

import java.util.Map;
import lombok.Getter;

@Getter
public class ParticipantUpdateEvent {

    private static final String PARTICIPANT_UPDATE_EVENT = "PARTICIPANT_UPDATE_EVENT";

    private final String type;
    private final Long sequence;
    private final String pickeatCode;
    private final Map<String, Boolean> completion;

    public ParticipantUpdateEvent(
            String pickeatCode,
            Map<String, Boolean> completion,
            Long sequence
    ) {
        this.type = PARTICIPANT_UPDATE_EVENT;
        this.sequence = sequence;
        this.pickeatCode = pickeatCode;
        this.completion = completion;
    }
}
