package com.pickeat.backend.support.fixture;

import com.pickeat.backend.restaurant.domain.RestaurantV2;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import java.util.List;

public class RestaurantsV2Fixture {

    public static RestaurantsV2 create(List<String> restaurantsNames) {
        List<RestaurantV2> restaurants = restaurantsNames.stream()
                .map(RestaurantV2Fixture::create)
                .toList();
        return new RestaurantsV2(restaurants);
    }
}
