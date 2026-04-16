package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Map;
import lombok.Getter;

@Getter
public class RestaurantLikeEvent {

    private final RestaurantEventType type;
    private final String pickeatCode;
    private final Map<String, Integer> likeCountByRestaurant;

    public RestaurantLikeEvent(String pickeatCode, Map<String, Integer> likeCountByRestaurant) {
        this.type = RestaurantEventType.RESTAURANT_LIKE_EVENT;
        this.pickeatCode = pickeatCode;
        this.likeCountByRestaurant = likeCountByRestaurant;
    }
}
