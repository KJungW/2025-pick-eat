package com.pickeat.backend.support.fixture;

import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.domain.FoodCategory;

public class RestaurantRequestFixture {

    public static RestaurantInfoDto create(String name) {
        return new RestaurantInfoDto(
                name,
                FoodCategory.KOREAN,
                10,
                "도로명 주소",
                "URL",
                "태그1,태그2",
                null,
                null
        );
    }
}
