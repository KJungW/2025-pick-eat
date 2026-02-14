package com.pickeat.backend.participant.application.dto;

import java.util.Map;

public record ParticipantStateDto(
        Map<String, Boolean> completionState
) {

}
