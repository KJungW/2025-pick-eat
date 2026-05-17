package com.pickeat.backend.restaurant.application.dto.external;

import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;

public record RestaurantSearchClientRequest(
        String category,
        Double x,
        Double y,
        Integer radius,
        Integer size
) {

    public static RestaurantSearchClientRequest of(
            LocationRestaurantRequest request,
            String category,
            int searchSize
    ) {
        return new RestaurantSearchClientRequest(
                category,
                request.x(),
                request.y(),
                request.radius(),
                searchSize
        );
    }
}
