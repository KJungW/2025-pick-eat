package com.pickeat.backend.pickeat.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "참여자 생성 요청")
public record ParticipantRequestV2(
        @Schema(description = "참여자 닉네임", example = "몽이")
        @NotBlank(message = "닉네임은 공백으로 입력할 수 없습니다.")
        String nickname,

        @Schema(description = "참여할 픽잇의 코드", example = "123")
        @NotBlank(message = "픽잇 코드는 빈 값일 수 없습니다.")
        String pickeatCode
) {

}
