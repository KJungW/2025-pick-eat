package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.TemplateRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.WishRestaurantRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantSearchFacade {

    private final LocationRestaurantSearchService locationRestaurantSearchService;
    private final WishRestaurantSearchService wishRestaurantSearchService;
    private final TemplateRestaurantSearchService templateRestaurantSearchService;
    private final RestaurantService restaurantService;


    public void searchByLocation(String pickeatCode, LocationRestaurantRequest request) {
        List<RestaurantRequest> restaurantRequests = locationRestaurantSearchService.searchByLocation(request);
        restaurantService.create(pickeatCode, restaurantRequests);
    }

    public void searchByWish(String pickeatCode, WishRestaurantRequest request) {
        List<RestaurantRequest> restaurantRequests = wishRestaurantSearchService.searchByWish(request);
        restaurantService.create(pickeatCode, restaurantRequests);
    }

    public void searchByTemplate(String pickeatCode, TemplateRestaurantRequest request) {
        List<RestaurantRequest> restaurantRequests = templateRestaurantSearchService.searchByTemplate(request);
        restaurantService.create(pickeatCode, restaurantRequests);
    }
}
