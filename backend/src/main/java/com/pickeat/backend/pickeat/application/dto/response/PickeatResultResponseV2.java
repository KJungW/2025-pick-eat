package com.pickeat.backend.pickeat.application.dto.response;

import com.pickeat.backend.pickeat.domain.PickeatResultV2;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;

@Schema(description = "픽잇 결과 응답")
public record PickeatResultResponseV2(
        @Schema(description = "식당 코드", example = "RWSDKK31412YE234")
        String code,
        @Schema(description = "식당 이름", example = "맛있는 한식당")
        String name,
        @Schema(description = "음식 카테고리", example = "한식")
        String category,
        @Schema(description = "도로명 주소", example = "서울 강남구 테헤란로 123")
        String roadAddressName,
        @Schema(description = "장소 URL", example = "https://place.map.kakao.com/12345")
        String placeUrl,
        @Schema(description = "식당 태그", example = "해물, 생선")
        List<String> tags,
        @Schema(description = "식당 사진 url")
        String pictureUrl
) {

    public static PickeatResultResponseV2 of(PickeatResultV2 pickeatResult) {
        return new PickeatResultResponseV2(
                pickeatResult.getCode(),
                pickeatResult.getName(),
                pickeatResult.getFoodCategory().getName(),
                pickeatResult.getRoadAddressName(),
                pickeatResult.getPlaceUrl(),
                parseTags(pickeatResult.getTags()),
                pickeatResult.getPictureUrl());
    }

    private static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }
}
