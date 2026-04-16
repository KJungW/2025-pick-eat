package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Map;

public record RestaurantLikeEventContent(
        Map<String, Integer> likeCountByRestaurant
) {

}
