package com.pickeat.backend.pickeat.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "픽잇 종료 여부 응답")
public record PickeatStateResponse(
        @Schema(description = "픽잇 종료 여부", example = "true")
        boolean isComplete
) {

}
