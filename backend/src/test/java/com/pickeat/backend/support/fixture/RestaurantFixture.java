package com.pickeat.backend.support.fixture;

import com.pickeat.backend.restaurant.domain.FoodCategory;
import com.pickeat.backend.restaurant.domain.Restaurant;

public class RestaurantFixture {

    public static Restaurant create(String name) {
        return new Restaurant(
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
