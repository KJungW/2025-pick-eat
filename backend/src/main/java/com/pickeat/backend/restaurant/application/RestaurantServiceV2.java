package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponseV2;
import com.pickeat.backend.restaurant.domain.RestaurantV2;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantServiceV2 {

    private final PickeatStorage pickeatStorage;
    private final RestaurantsStorage restaurantsStorage;

    public void create(List<RestaurantRequest> restaurantRequests, String pickeatCode) {
        PickeatV2 pickeat = getPickeatByPickeat(pickeatCode);
        RestaurantsV2 restaurants = convertToRestaurants(restaurantRequests);
        saveRestaurants(restaurants, pickeat.getCode());
    }

    public List<RestaurantResponseV2> getByPickeat(String pickeatCode) {
        PickeatV2 pickeat = getPickeatByPickeat(pickeatCode);
        RestaurantsV2 restaurants = getRestaurantByPickeat(pickeatCode);
        return RestaurantResponseV2.of(restaurants);
    }

    private PickeatV2 getPickeatByPickeat(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PICKEAT_NOT_FOUND));
    }

    private RestaurantsV2 getRestaurantByPickeat(String pickeatCode) {
        return restaurantsStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private void saveRestaurants(RestaurantsV2 restaurants, String pickeatCode) {
        Boolean isSuccess = restaurantsStorage.saveIfAbsent(restaurants, pickeatCode);
        if (!Boolean.TRUE.equals(isSuccess)) {
            throw new BusinessException(ErrorCode.RESTAURANT_ALREADY_EXISTS);
        }
    }

    private RestaurantsV2 convertToRestaurants(List<RestaurantRequest> restaurantRequests) {
        List<RestaurantV2> restaurants = restaurantRequests.stream()
                .map(RestaurantRequest::toRestaurantV2)
                .toList();
        return new RestaurantsV2(restaurants);
    }
}
