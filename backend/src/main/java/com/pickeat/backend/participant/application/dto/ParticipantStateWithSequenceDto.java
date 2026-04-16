package com.pickeat.backend.participant.application.dto;

import java.util.Map;

public record ParticipantStateWithSequenceDto(
        Long sequence,
        Map<String, Boolean> completionState
) {

}

