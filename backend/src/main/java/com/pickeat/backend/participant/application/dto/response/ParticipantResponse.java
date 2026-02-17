package com.pickeat.backend.participant.application.dto.response;

import com.pickeat.backend.participant.domain.Participant;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "참가자 응답")
public record ParticipantResponse(

        @Schema(description = "참가자 코드", example = "participand-code")
        String participantCode,

        @Schema(description = "참가자 닉네임", example = "김에드")
        String nickname
) {

    public static ParticipantResponse from(Participant participant) {
        return new ParticipantResponse(
                participant.getCode(),
                participant.getNickname()
        );
    }

    public static List<ParticipantResponse> from(List<Participant> participants) {
        return participants.stream().map(ParticipantResponse::from).toList();
    }
}
