package com.pickeat.backend.restaurant.application.dto.response;

import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.Set;

@Schema(description = "식당 상태 응답")
public record RestaurantStateResponse(
        @Schema(description = "소거되지 않은 식당 코드")
        Set<String> aliveRestaurantCode,
        @Schema(description = "식당 별 좋아요 합계 (식당코드:좋아요합계)")
        Map<String, Integer> likeCountByRestaurant
) {

    public static RestaurantStateResponse of(RestaurantStateDto dto) {
        return new RestaurantStateResponse(dto.aliveRestaurantCode(), dto.likeCountByRestaurant());
    }
}
