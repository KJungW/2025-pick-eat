package com.pickeat.backend.restaurant.application.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestaurantEventType {
    RESTAURANT_EXCLUDE_EVENT("RESTAURANT_EXCLUDE_EVENT"),
    RESTAURANT_LIKE_EVENT("RESTAURANT_LIKE_EVENT");

    private final String value;
}
