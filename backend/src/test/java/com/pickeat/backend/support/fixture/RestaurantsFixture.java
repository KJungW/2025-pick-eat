package com.pickeat.backend.support.fixture;

import com.pickeat.backend.restaurant.domain.Restaurant;
import com.pickeat.backend.restaurant.domain.Restaurants;
import java.util.List;

public class RestaurantsFixture {

    public static Restaurants create(List<String> restaurantsNames) {
        List<Restaurant> restaurants = restaurantsNames.stream()
                .map(RestaurantFixture::create)
                .toList();
        return new Restaurants(restaurants);
    }
}
