package com.pickeat.backend.participant.application.dto.event;

import java.util.Map;

public record ParticipantUpdateEventContent(
        Map<String, Boolean> completionState
) {

}
