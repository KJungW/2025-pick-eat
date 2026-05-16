package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantUpdateEventRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponse;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantStateResponse;
import com.pickeat.backend.restaurant.domain.Restaurant;
import com.pickeat.backend.restaurant.domain.Restaurants;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RestaurantService {

    private final PickeatStorage pickeatStorage;
    private final RestaurantsStorage restaurantsStorage;
    private final ApplicationEventPublisher eventPublisher;

    public void create(String pickeatCode, List<RestaurantRequest> restaurantRequests) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        Restaurants restaurants = convertToRestaurants(restaurantRequests);
        setupRestaurants(pickeatCode, restaurants);
    }

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

    public void exclude(String pickeatCode, List<String> restaurantCodes) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        excludeRestaurants(pickeatCode, restaurantCodes);
        eventPublisher.publishEvent(new RestaurantUpdateEventRequest(pickeatCode));
    }

    public void like(String pickeatCode, String participantCode, String restaurantCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        likeRestaurant(pickeatCode, participantCode, restaurantCode);
        eventPublisher.publishEvent(new RestaurantUpdateEventRequest(pickeatCode));
    }

    public void cancelLike(String pickeatCode, String participantCode, String restaurantCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        cancelLikeRestaurant(pickeatCode, participantCode, restaurantCode);
        eventPublisher.publishEvent(new RestaurantUpdateEventRequest(pickeatCode));
    }

    private Pickeat getPickeatByCode(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND));
    }

    private Restaurants getRestaurantMetaInPickeat(String pickeatCode) {
        return restaurantsStorage.getRestaurantMeta(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private RestaurantStateDto getRestaurantStateInPickeat(String pickeatCode) {
        return restaurantsStorage.getRestaurantState(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private void setupRestaurants(String pickeatCode, Restaurants restaurants) {
        boolean isSuccess = restaurantsStorage.setupRestaurants(pickeatCode, restaurants);
        if (!isSuccess) {
            throw new BusinessException(ErrorCode.RESTAURANT_ALREADY_EXISTS);
        }
    }

    private void excludeRestaurants(String pickeatCode, List<String> restaurantCodes) {
        restaurantsStorage.excludeRestaurants(pickeatCode, restaurantCodes);
    }

    private void likeRestaurant(String pickeatCode, String participantCode, String restaurantCode) {
        boolean isSuccess = restaurantsStorage.like(pickeatCode, participantCode, restaurantCode);
        if (!isSuccess) {
            throw new BusinessException(ErrorCode.PARTICIPANT_RESTAURANT_ALREADY_LIKED);
        }
    }

    private void cancelLikeRestaurant(String pickeatCode, String participantCode, String restaurantCode) {
        boolean isSuccess = restaurantsStorage.cancelLike(pickeatCode, participantCode, restaurantCode);
        if (!isSuccess) {
            throw new BusinessException(ErrorCode.PARTICIPANT_RESTAURANT_NOT_LIKED);
        }
    }

    private Restaurants convertToRestaurants(List<RestaurantRequest> restaurantRequests) {
        List<Restaurant> restaurants = restaurantRequests.stream()
                .map(RestaurantRequest::toRestaurant)
                .toList();
        return new Restaurants(restaurants);
    }
}
