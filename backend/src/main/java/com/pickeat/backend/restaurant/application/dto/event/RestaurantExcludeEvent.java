package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Set;
import lombok.Getter;

@Getter
public class RestaurantExcludeEvent {

    private final RestaurantEventType type;
    private final String pickeatCode;
    private final Set<String> aliveRestaurantIds;

    public RestaurantExcludeEvent(String pickeatCode, Set<String> aliveRestaurantIds) {
        this.type = RestaurantEventType.RESTAURANT_EXCLUDE_EVENT;
        this.pickeatCode = pickeatCode;
        this.aliveRestaurantIds = aliveRestaurantIds;
    }
}
