package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Set;
import lombok.Getter;

@Getter
public class RestaurantExcludeEvent {

    private static final String RESTAURANT_EXCLUDE_EVENT = "RESTAURANT_EXCLUDE_EVENT";

    private final String type;
    private final Long sequence;
    private final String pickeatCode;
    private final Set<String> aliveRestaurantIds;

    public RestaurantExcludeEvent(
            String pickeatCode,
            Set<String> aliveRestaurantIds,
            Long sequence
    ) {
        this.type = RESTAURANT_EXCLUDE_EVENT;
        this.sequence = sequence;
        this.pickeatCode = pickeatCode;
        this.aliveRestaurantIds = aliveRestaurantIds;
    }
}
