package com.pickeat.backend.restaurant.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class RestaurantV2 {

    private final String code;
    private final String name;
    private final FoodCategory foodCategory;
    private final String roadAddressName;
    private final String placeUrl;
    private final String tags;
    private final String pictureKey;
    private final String pictureUrl;

    public RestaurantV2(
            String name,
            FoodCategory foodCategory,
            String roadAddressName,
            String placeUrl,
            String tags,
            String pictureKey,
            String pictureUrl
    ) {
        this.code = UUID.randomUUID().toString();
        this.name = name;
        this.foodCategory = foodCategory;
        this.roadAddressName = roadAddressName;
        this.placeUrl = placeUrl;
        this.tags = tags;
        this.pictureKey = pictureKey;
        this.pictureUrl = pictureUrl;
    }
}
