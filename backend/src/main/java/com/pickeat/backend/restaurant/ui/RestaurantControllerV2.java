package com.pickeat.backend.restaurant.ui;

import com.pickeat.backend.global.auth.annotation.ParticipantInPickeatV2;
import com.pickeat.backend.global.auth.principal.ParticipantPrincipalV2;
import com.pickeat.backend.restaurant.application.RestaurantSearchFacadeV2;
import com.pickeat.backend.restaurant.application.RestaurantServiceV2;
import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.TemplateRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.WishRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponseV2;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class RestaurantControllerV2 {

    private final RestaurantServiceV2 restaurantService;
    private final RestaurantSearchFacadeV2 restaurantSearchFacade;

    @PostMapping("/pickeats/{pickeatCode}/restaurants/location/new")
    public ResponseEntity<Void> createRestaurantsByLocation(
            @PathVariable("pickeatCode") String pickeatCode,
            @Valid @RequestBody LocationRestaurantRequest request
    ) {
        restaurantSearchFacade.searchByLocation(request, pickeatCode);
        URI location = URI.create("/pickeats/" + pickeatCode + "/restaurants");
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/pickeats/{pickeatCode}/restaurants/wish/new")
    public ResponseEntity<Void> createRestaurantsByWish(
            @PathVariable("pickeatCode") String pickeatCode,
            @Valid @RequestBody WishRestaurantRequest request
    ) {
        restaurantSearchFacade.searchByWish(request, pickeatCode);
        URI location = URI.create("/pickeats/" + pickeatCode + "/restaurants");
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/pickeats/{pickeatCode}/restaurants/template/new")
    public ResponseEntity<Void> createRestaurantsByTemplate(
            @PathVariable("pickeatCode") String pickeatCode,
            @Valid @RequestBody TemplateRestaurantRequest request
    ) {
        restaurantSearchFacade.searchByTemplate(request, pickeatCode);
        URI location = URI.create("/pickeats/" + pickeatCode + "/restaurants");
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/pickeats/restaurants/new")
    public ResponseEntity<List<RestaurantResponseV2>> getPickeatRestaurants(
            @ParticipantInPickeatV2 ParticipantPrincipalV2 principal
    ) {
        List<RestaurantResponseV2> response = restaurantService.getByPickeat(principal.pickeatCode());
        return ResponseEntity.ok().body(response);
    }
}
