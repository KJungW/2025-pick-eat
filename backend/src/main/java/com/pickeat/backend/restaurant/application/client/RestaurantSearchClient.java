package com.pickeat.backend.restaurant.application.client;

import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.external.RestaurantSearchClientRequest;
import java.util.List;

public interface RestaurantSearchClient {

    List<RestaurantInfoDto> getRestaurants(RestaurantSearchClientRequest request);
}
