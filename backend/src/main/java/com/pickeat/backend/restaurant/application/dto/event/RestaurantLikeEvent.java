package com.pickeat.backend.restaurant.application.dto.event;

import java.util.Map;
import lombok.Getter;

@Getter
public class RestaurantLikeEvent {

    private static final String RESTAURANT_LIKE_EVENT = "RESTAURANT_LIKE_EVENT";

    private final String type;
    private final Long sequence;
    private final String pickeatCode;
    private final Map<String, Integer> likeCountByRestaurant;

    public RestaurantLikeEvent(
            String pickeatCode,
            Map<String, Integer> likeCountByRestaurant,
            Long sequence
    ) {
        this.type = RESTAURANT_LIKE_EVENT;
        this.sequence = sequence;
        this.pickeatCode = pickeatCode;
        this.likeCountByRestaurant = likeCountByRestaurant;
    }
}
