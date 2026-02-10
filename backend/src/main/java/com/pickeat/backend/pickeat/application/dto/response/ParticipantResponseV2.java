package com.pickeat.backend.pickeat.application.dto.response;

import com.pickeat.backend.pickeat.domain.ParticipantV2;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참가자 응답")
public record ParticipantResponseV2(

        @Schema(description = "참가자 코드", example = "participand-code")
        String participantCode,

        @Schema(description = "참가자 닉네임", example = "김에드")
        String nickname
) {

    public static ParticipantResponseV2 from(ParticipantV2 participant) {
        return new ParticipantResponseV2(
                participant.getCode(),
                participant.getNickname()
        );
    }
}
