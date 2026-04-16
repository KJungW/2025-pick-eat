package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Map;

public class RestaurantLikeEvent {

    private final RestaurantEventType type;
    private final Map<String, Integer> likeCountByRestaurant;

    public RestaurantLikeEvent(Map<String, Integer> likeCountByRestaurant) {
        this.type = RestaurantEventType.RESTAURANT_LIKE_EVENT;
        this.likeCountByRestaurant = likeCountByRestaurant;
    }
}
