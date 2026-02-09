package com.pickeat.backend.pickeat.application.dto.response;

import com.pickeat.backend.pickeat.domain.PickeatV2;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "픽잇 응답")
public record PickeatResponseV2(
        @Schema(description = "픽잇 코드 (UUID)", example = "abc123de-f456-789g-hijk-lmnopqrstuvw")
        String code,

        @Schema(description = "픽잇 이름", example = "점심 맛집 찾기")
        String name
) {

    public static PickeatResponseV2 from(PickeatV2 pickeat) {
        return new PickeatResponseV2(pickeat.getCode(), pickeat.getName());
    }

    public static List<PickeatResponseV2> from(List<PickeatV2> pickeats) {
        return pickeats.stream().map(PickeatResponseV2::from).toList();
    }
}
