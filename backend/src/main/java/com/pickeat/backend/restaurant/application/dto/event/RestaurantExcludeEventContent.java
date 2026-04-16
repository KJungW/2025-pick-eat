package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Set;

public record RestaurantExcludeEventContent(
        Set<String> aliveRestaurantIds
) {

}
