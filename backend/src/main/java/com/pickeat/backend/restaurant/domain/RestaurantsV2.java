package com.pickeat.backend.restaurant.domain;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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

    public RestaurantV2 selectRestaurant(RestaurantStateDto restaurantState) {
        if (restaurantState.hasNoAliveRestaurants()) {
            return randomSelectRestaurant();
        }
        List<String> topRatedRestaurantCodes = restaurantState.extrudeMaxLikeRestaurantCode();
        return randomSelectTopRatedRestaurant(topRatedRestaurantCodes);
    }

    private RestaurantV2 randomSelectRestaurant() {
        return restaurants.get(ThreadLocalRandom.current().nextInt(restaurants.size()));
    }

    private RestaurantV2 randomSelectTopRatedRestaurant(List<String> topRatedRestaurantCodes) {
        String randomSelectedCode = topRatedRestaurantCodes
                .get(ThreadLocalRandom.current().nextInt(topRatedRestaurantCodes.size()));
        return findBySelectedCode(randomSelectedCode);
    }

    private RestaurantV2 findBySelectedCode(String selectedCode) {
        return restaurants.stream()
                .filter(restaurant -> restaurant.getCode().equals(selectedCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
