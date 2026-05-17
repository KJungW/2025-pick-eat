package com.pickeat.backend.restaurant.application.facade;

import com.pickeat.backend.restaurant.application.LocationRestaurantSearchService;
import com.pickeat.backend.restaurant.application.RestaurantCommandService;
import com.pickeat.backend.restaurant.application.TemplateRestaurantSearchService;
import com.pickeat.backend.restaurant.application.WishRestaurantSearchService;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
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
    private final RestaurantCommandService restaurantService;

    public void searchByLocation(String pickeatCode, LocationRestaurantRequest request) {
        List<RestaurantInfoDto> restaurantInfoDtos = locationRestaurantSearchService.searchByLocation(request);
        restaurantService.create(pickeatCode, restaurantInfoDtos);
    }

    public void searchByWish(String pickeatCode, WishRestaurantRequest request) {
        List<RestaurantInfoDto> restaurantInfoDtos = wishRestaurantSearchService.searchByWish(request);
        restaurantService.create(pickeatCode, restaurantInfoDtos);
    }

    public void searchByTemplate(String pickeatCode, TemplateRestaurantRequest request) {
        List<RestaurantInfoDto> restaurantInfoDtos = templateRestaurantSearchService.searchByTemplate(request);
        restaurantService.create(pickeatCode, restaurantInfoDtos);
    }
}
