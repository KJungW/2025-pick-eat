package com.pickeat.backend.restaurant.application.dto;

import java.util.Map;
import java.util.Set;

public record RestaurantStateDto(
        Set<String> aliveRestaurantCode,
        Map<String, Integer> likeCountByRestaurant
) {

}
