package com.pickeat.backend.support.fake.restaurant;

import com.pickeat.backend.restaurant.application.client.RestaurantSearchClient;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.external.RestaurantSearchClientRequest;
import com.pickeat.backend.restaurant.domain.FoodCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FakeRestaurantSearchClient implements RestaurantSearchClient {

    @Override
    public List<RestaurantInfoDto> getRestaurants(RestaurantSearchClientRequest request) {
        List<RestaurantInfoDto> restaurants = new ArrayList<>();
        for (int i = 0; i < request.size(); i++) {
            restaurants.add(RestaurantInfoDto.fromLocation(
                    request.category() + "음식" + i,
                    FoodCategory.parse(request.category()),
                    ThreadLocalRandom.current().nextInt(0, request.radius() + 1),
                    "도로명 주소" + i,
                    "식당 URL" + i,
                    "태그" + i
            ));
        }
        return restaurants;
    }
}
