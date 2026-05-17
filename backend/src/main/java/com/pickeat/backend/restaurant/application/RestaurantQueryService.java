package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponse;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantStateResponse;
import com.pickeat.backend.restaurant.domain.Restaurants;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsCommandStorage;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsQueryStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantQueryService {

    private final PickeatStorage pickeatStorage;
    private final RestaurantsQueryStorage restaurantsQueryStorage;
    private final RestaurantsCommandStorage restaurantsCommandStorage;

    public List<RestaurantResponse> getMetaInPickeat(String pickeatCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        Restaurants restaurants = getRestaurantMetaInPickeat(pickeatCode);
        return RestaurantResponse.of(restaurants);
    }

    public RestaurantStateResponse getStateInPickeat(String pickeatCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        RestaurantStateDto restaurantState = getRestaurantStateInPickeat(pickeatCode);
        return RestaurantStateResponse.of(restaurantState);
    }

    private Pickeat getPickeatByCode(String pickeatCode) {
        return pickeatStorage.getMeta(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.PICKEAT_NOT_FOUND));
    }

    private Restaurants getRestaurantMetaInPickeat(String pickeatCode) {
        return restaurantsQueryStorage.getRestaurantMeta(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.RESTAURANT_NOT_FOUND));
    }

    private RestaurantStateDto getRestaurantStateInPickeat(String pickeatCode) {
        return restaurantsQueryStorage.getRestaurantState(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.RESTAURANT_NOT_FOUND));
    }
}
