package com.pickeat.backend.participant.application.dto;

import java.util.Map;

public record ParticipantStateDto(
        Long sequence,
        Map<String, Boolean> completionState
) {

}

