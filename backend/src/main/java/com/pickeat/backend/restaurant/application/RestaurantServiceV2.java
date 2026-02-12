package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponseV2;
import com.pickeat.backend.restaurant.domain.RestaurantV2;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import com.pickeat.backend.restaurant.domain.storage.RestaurantExcludedStorage;
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
    private final RestaurantExcludedStorage excludedStorage;

    public void create(List<RestaurantRequest> restaurantRequests, String pickeatCode) {
        PickeatV2 pickeat = getPickeatByCode(pickeatCode);
        RestaurantsV2 restaurants = convertToRestaurants(restaurantRequests);
        saveRestaurants(pickeat.getCode(), restaurants);
        setupRestaurantExclude(pickeat.getCode(), restaurants);
    }

    public List<RestaurantResponseV2> getByPickeat(String pickeatCode) {
        PickeatV2 pickeat = getPickeatByCode(pickeatCode);
        RestaurantsV2 restaurants = getRestaurantByPickeat(pickeatCode);
        return RestaurantResponseV2.of(restaurants);
    }

    public void exclude(String pickeatCode, List<String> restaurantCodes) {
        PickeatV2 pickeat = getPickeatByCode(pickeatCode);
        excludedStorage.exclude(pickeatCode, restaurantCodes);
    }

    private PickeatV2 getPickeatByCode(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PICKEAT_NOT_FOUND));
    }

    private RestaurantsV2 getRestaurantByPickeat(String pickeatCode) {
        return restaurantsStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private void saveRestaurants(String pickeatCode, RestaurantsV2 restaurants) {
        Boolean isSuccess = restaurantsStorage.saveIfAbsent(restaurants, pickeatCode);
        if (!Boolean.TRUE.equals(isSuccess)) {
            throw new BusinessException(ErrorCode.RESTAURANT_ALREADY_EXISTS);
        }
    }

    private void setupRestaurantExclude(String pickeatCode, RestaurantsV2 restaurants) {
        List<String> restaurantCodes = restaurants.extrudeRestaurantCodes();
        boolean isSuccess = excludedStorage.setupExclude(pickeatCode, restaurantCodes);
        if (!isSuccess) {
            restaurantsStorage.remove(pickeatCode);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private RestaurantsV2 convertToRestaurants(List<RestaurantRequest> restaurantRequests) {
        List<RestaurantV2> restaurants = restaurantRequests.stream()
                .map(RestaurantRequest::toRestaurantV2)
                .toList();
        return new RestaurantsV2(restaurants);
    }
}
