package com.pickeat.backend.template.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "템플릿 페이지 요청")
public record TemplatePageRequest(
        @Schema(description = "커서로 활용할 템플릿 ID", example = "0")
        @NotNull(message = "커서로 활용할 템플릿 ID는 NULL일 수 없습니다.")
        Long startId,

        @Schema(description = "페이지 요소 개수", example = "10")
        @NotNull(message = "페이지 요소 개수는 NULL일 수 없습니다.")
        @Min(value = 1, message = "페이지 요소 개수는 1개 이상이어야 합니다.")
        @Max(value = 50, message = "페이지 요소 개수는 50개 이하여야 합니다.")
        Integer size
) {

}
