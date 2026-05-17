package com.pickeat.backend.restaurant.infrastructure.search.fake;

import com.pickeat.backend.restaurant.application.client.RestaurantSearchClient;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.external.RestaurantSearchClientRequest;
import com.pickeat.backend.restaurant.domain.FoodCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class FakeRestaurantSearchClient implements RestaurantSearchClient {

    private static final int GENERAL_PROCESSING_TIME = 70;

    @Override
    public List<RestaurantInfoDto> getRestaurants(RestaurantSearchClientRequest request) {

        long startTime = System.nanoTime();

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

        long endTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        long remainingTime = GENERAL_PROCESSING_TIME - endTime;

        if (remainingTime > 0) {
            try {
                Thread.sleep(remainingTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return restaurants;
    }
}
