package com.pickeat.backend.support.fixture;

import com.pickeat.backend.restaurant.domain.FoodCategory;
import com.pickeat.backend.restaurant.domain.RestaurantV2;

public class RestaurantV2Fixture {

    public static RestaurantV2 create(String name) {
        return new RestaurantV2(
                name,
                FoodCategory.KOREAN,
                "도로명 주소",
                "URL",
                "태그1,태그2",
                null,
                null
        );
    }
}
