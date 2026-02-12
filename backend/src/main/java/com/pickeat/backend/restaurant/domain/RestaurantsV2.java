package com.pickeat.backend.restaurant.domain;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class RestaurantsV2 {

    private final List<RestaurantV2> restaurants;

    public RestaurantsV2(List<RestaurantV2> restaurants) {
        this.restaurants = List.copyOf(restaurants);
    }

    public List<String> extrudeRestaurantCodes() {
        return restaurants.stream().map(RestaurantV2::getCode).toList();
    }
}
