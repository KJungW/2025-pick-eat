package com.pickeat.backend.pickeat.application.dto.event;

import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;

public record PickeatCompletionEventRequest(
        String pickeatCode,
        PickeatResultResponse pickeatResult
) {

}
