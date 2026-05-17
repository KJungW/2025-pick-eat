package com.pickeat.backend.participant.application.dto.event;

import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import java.util.Map;

public record ParticipantUpdateEventRequest(
        Long sequence,
        String pickeatCode,
        Map<String, Boolean> completionState
) {

    public static ParticipantUpdateEventRequest of(ParticipantStateDto state, String pickeatCode) {
        return new ParticipantUpdateEventRequest(
                state.sequence(),
                pickeatCode,
                state.completionState()
        );
    }
}
