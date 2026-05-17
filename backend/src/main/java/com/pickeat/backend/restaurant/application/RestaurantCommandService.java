package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantUpdateEventRequest;
import com.pickeat.backend.restaurant.domain.Restaurant;
import com.pickeat.backend.restaurant.domain.Restaurants;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsCommandStorage;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsQueryStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantCommandService {

    private final PickeatStorage pickeatStorage;
    private final RestaurantsQueryStorage restaurantsQueryStorage;
    private final RestaurantsCommandStorage restaurantsCommandStorage;
    private final ApplicationEventPublisher eventPublisher;

    public void create(String pickeatCode, List<RestaurantInfoDto> restaurantInfoDtos) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        Restaurants restaurants = convertToRestaurants(restaurantInfoDtos);
        setupRestaurants(pickeatCode, restaurants);
    }

    public void exclude(String pickeatCode, List<String> restaurantCodes) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        excludeRestaurants(pickeatCode, restaurantCodes);
        publishRestaurantUpdateEvent(pickeatCode);
    }

    public void like(String pickeatCode, String participantCode, String restaurantCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        likeRestaurant(pickeatCode, participantCode, restaurantCode);
        publishRestaurantUpdateEvent(pickeatCode);
    }

    public void cancelLike(String pickeatCode, String participantCode, String restaurantCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        cancelLikeRestaurant(pickeatCode, participantCode, restaurantCode);
        publishRestaurantUpdateEvent(pickeatCode);
    }

    private Pickeat getPickeatByCode(String pickeatCode) {
        return pickeatStorage.getMeta(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.PICKEAT_NOT_FOUND));
    }

    private RestaurantStateDto getRestaurantStateInPickeat(String pickeatCode) {
        return restaurantsQueryStorage.getRestaurantState(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.RESTAURANT_NOT_FOUND));
    }

    private RestaurantStateDto getRestaurantStateWithSequence(String pickeatCode) {
        return restaurantsQueryStorage.getRestaurantStateWithSequence(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.RESTAURANT_NOT_FOUND));
    }

    private void setupRestaurants(String pickeatCode, Restaurants restaurants) {
        boolean isSuccess = restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);
        if (!isSuccess) {
            throw new ClientException(ClientErrorCode.RESTAURANT_ALREADY_EXISTS);
        }
    }

    private void excludeRestaurants(String pickeatCode, List<String> restaurantCodes) {
        restaurantsCommandStorage.excludeRestaurants(pickeatCode, restaurantCodes);
    }

    private void likeRestaurant(String pickeatCode, String participantCode, String restaurantCode) {
        boolean isSuccess = restaurantsCommandStorage.like(pickeatCode, participantCode, restaurantCode);
        if (!isSuccess) {
            throw new ClientException(ClientErrorCode.PARTICIPANT_RESTAURANT_ALREADY_LIKED);
        }
    }

    private void cancelLikeRestaurant(String pickeatCode, String participantCode, String restaurantCode) {
        boolean isSuccess = restaurantsCommandStorage.cancelLike(pickeatCode, participantCode, restaurantCode);
        if (!isSuccess) {
            throw new ClientException(ClientErrorCode.PARTICIPANT_RESTAURANT_NOT_LIKED);
        }
    }

    private Restaurants convertToRestaurants(List<RestaurantInfoDto> restaurantInfoDtos) {
        List<Restaurant> restaurants = restaurantInfoDtos.stream()
                .map(RestaurantInfoDto::toRestaurant)
                .toList();
        return new Restaurants(restaurants);
    }

    private void publishRestaurantUpdateEvent(String pickeatCode) {
        RestaurantStateDto state = getRestaurantStateWithSequence(pickeatCode);
        eventPublisher.publishEvent(new RestaurantUpdateEventRequest(pickeatCode, state));
    }
}
