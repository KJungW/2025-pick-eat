package com.pickeat.backend.restaurant.application.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record RestaurantStateDto(
        Long sequence,
        Set<String> aliveRestaurantCode,
        Map<String, Integer> likeCountByRestaurant
) {

    public List<String> getMostLikedRestaurantCodes() {
        int maxLikeCount = aliveRestaurantCode.stream()
                .filter(likeCountByRestaurant::containsKey)
                .mapToInt(likeCountByRestaurant::get)
                .max()
                .orElse(0);
        return aliveRestaurantCode.stream()
                .filter(code -> likeCountByRestaurant.getOrDefault(code, 0) == maxLikeCount)
                .toList();
    }

    public boolean hasNoAliveRestaurants() {
        return aliveRestaurantCode.isEmpty();
    }
}
