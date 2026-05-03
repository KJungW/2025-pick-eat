package com.pickeat.backend.restaurant.application.dto.event;

import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import java.util.Map;
import java.util.Set;

public record RestaurantUpdateEventContent(
        Set<String> aliveRestaurantIds,
        Map<String, Integer> likeCountByRestaurant
) {

    public static RestaurantUpdateEventContent of(RestaurantStateDto dto) {
        return new RestaurantUpdateEventContent(dto.aliveRestaurantCode(), dto.likeCountByRestaurant());
    }
}
