package com.pickeat.backend.restaurant.domain;

import com.pickeat.backend.global.exception.code.ServerErrorCode;
import com.pickeat.backend.global.exception.type.ServerException;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Restaurants {

    private final List<Restaurant> restaurants;

    public Restaurants(List<Restaurant> restaurants) {
        this.restaurants = List.copyOf(restaurants);
    }

    public List<String> extrudeRestaurantCodes() {
        return restaurants.stream().map(Restaurant::getCode).toList();
    }

    public Restaurant selectRestaurant(RestaurantStateDto restaurantState) {
        if (restaurantState.hasNoAliveRestaurants()) {
            return randomSelectRestaurant();
        }
        List<String> topRatedRestaurantCodes = restaurantState.getMostLikedRestaurantCodes();
        return randomSelectTopRatedRestaurant(topRatedRestaurantCodes);
    }

    private Restaurant randomSelectRestaurant() {
        return restaurants.get(ThreadLocalRandom.current().nextInt(restaurants.size()));
    }

    private Restaurant randomSelectTopRatedRestaurant(List<String> topRatedRestaurantCodes) {
        String randomSelectedCode = topRatedRestaurantCodes
                .get(ThreadLocalRandom.current().nextInt(topRatedRestaurantCodes.size()));
        return findBySelectedCode(randomSelectedCode);
    }

    private Restaurant findBySelectedCode(String selectedCode) {
        return restaurants.stream()
                .filter(restaurant -> restaurant.getCode().equals(selectedCode))
                .findFirst()
                .orElseThrow(() -> new ServerException(ServerErrorCode.INTERNAL_SERVER_ERROR));
    }
}
