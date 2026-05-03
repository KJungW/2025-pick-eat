package com.pickeat.backend.participant.application.dto.response;

import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "참여자 상태 응답")
public record ParticipantStateResponse(
        @Schema(description = "시퀀스 번호")
        Long sequence,
        @Schema(description = "각 참가지의 투표 완료 여부")
        Map<String, Boolean> completion
) {

    public static ParticipantStateResponse from(ParticipantStateDto dto) {
        return new ParticipantStateResponse(dto.sequence(), dto.completionState());
    }
}
