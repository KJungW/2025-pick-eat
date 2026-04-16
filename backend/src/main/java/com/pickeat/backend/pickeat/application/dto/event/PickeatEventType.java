package com.pickeat.backend.pickeat.application.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickeatEventType {

    PICKEAT_COMPLETION_EVENT("PICKEAT_COMPLETION_EVENT");

    private final String value;
}
