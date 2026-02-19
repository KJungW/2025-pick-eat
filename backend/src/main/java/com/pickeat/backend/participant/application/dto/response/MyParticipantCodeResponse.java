package com.pickeat.backend.participant.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 참가자 코드 조회")
public record MyParticipantCodeResponse(
        @Schema(description = "나의 참가자 코드", example = "participand-code")
        String participantCode
) {

}
