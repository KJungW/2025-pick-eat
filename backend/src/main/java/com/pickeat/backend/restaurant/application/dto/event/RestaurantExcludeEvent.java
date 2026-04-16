package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Set;

public class RestaurantExcludeEvent {

    private final RestaurantEventType type;
    private final Set<String> aliveRestaurantIds;

    public RestaurantExcludeEvent(Set<String> aliveRestaurantIds) {
        this.type = RestaurantEventType.RESTAURANT_EXCLUDE_EVENT;
        this.aliveRestaurantIds = aliveRestaurantIds;
    }
}
