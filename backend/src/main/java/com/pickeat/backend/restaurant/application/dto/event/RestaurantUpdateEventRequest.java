package com.pickeat.backend.restaurant.application.dto.event;

import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;

public record RestaurantUpdateEventRequest(
        String pickeatCode,
        RestaurantStateDto state
) {

}
