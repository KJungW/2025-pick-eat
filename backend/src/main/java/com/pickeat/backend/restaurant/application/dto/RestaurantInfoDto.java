package com.pickeat.backend.restaurant.application.dto;

import com.pickeat.backend.restaurant.domain.FoodCategory;
import com.pickeat.backend.restaurant.domain.Picture;
import com.pickeat.backend.restaurant.domain.Restaurant;
import com.pickeat.backend.restaurant.domain.RestaurantInfo;
import com.pickeat.backend.template.domain.TemplateWish;
import com.pickeat.backend.wish.domain.Wish;
import java.util.List;

public record RestaurantInfoDto(
        String name,
        FoodCategory category,
        Integer distance,
        String roadAddressName,
        String placeUrl,
        String tags,
        String pictureKey,
        String pictureUrl
) {

    public static RestaurantInfoDto fromLocation(
            String name,
            FoodCategory category,
            Integer distance,
            String roadAddressName,
            String placeUrl,
            String tags
    ) {
        return new RestaurantInfoDto(
                name,
                category,
                distance,
                roadAddressName,
                placeUrl,
                tags,
                null,
                null
        );
    }

    public static List<RestaurantInfoDto> fromWishes(List<Wish> wishes) {
        return wishes.stream()
                .map(Wish::getRestaurantInfo)
                .map(RestaurantInfoDto::createRestaurantInfoDto)
                .toList();
    }

    public static List<RestaurantInfoDto> fromTemplateWish(List<TemplateWish> templateWishes) {
        return templateWishes.stream()
                .map(TemplateWish::getRestaurantInfo)
                .map(RestaurantInfoDto::createRestaurantInfoDto)
                .toList();
    }

    private static RestaurantInfoDto createRestaurantInfoDto(RestaurantInfo restaurantInfo) {
        Picture picture = restaurantInfo.getPicture();
        return new RestaurantInfoDto(
                restaurantInfo.getName(),
                restaurantInfo.getFoodCategory(),
                restaurantInfo.getDistance(),
                restaurantInfo.getRoadAddressName(),
                restaurantInfo.getPlaceUrl(),
                restaurantInfo.getTags(),
                picture == null ? null : picture.getPictureKey(),
                picture == null ? null : picture.getPictureUrl()
        );
    }

    public Restaurant toRestaurant() {
        return new Restaurant(
                this.name,
                this.category,
                this.roadAddressName,
                this.placeUrl,
                this.tags,
                this.pictureKey,
                this.pictureUrl
        );
    }
}
