package com.pickeat.backend.participant.application.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParticipantEventType {

    PARTICIPANT_UPDATE_EVENT("PARTICIPANT_UPDATE_EVENT");

    private final String value;
}
