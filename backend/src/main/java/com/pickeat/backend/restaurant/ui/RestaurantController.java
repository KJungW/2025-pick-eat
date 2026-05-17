package com.pickeat.backend.restaurant.ui;

import com.pickeat.backend.global.argument.annotation.Participant;
import com.pickeat.backend.global.argument.principal.ParticipantPrincipal;
import com.pickeat.backend.restaurant.application.RestaurantCommandService;
import com.pickeat.backend.restaurant.application.RestaurantQueryService;
import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantExcludeRequest;
import com.pickeat.backend.restaurant.application.dto.request.TemplateRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.WishRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponse;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantStateResponse;
import com.pickeat.backend.restaurant.application.facade.RestaurantSearchFacade;
import com.pickeat.backend.restaurant.ui.api.RestaurantApiSpec;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class RestaurantController implements RestaurantApiSpec {

    private final RestaurantQueryService restaurantQueryService;
    private final RestaurantCommandService restaurantCommandService;
    private final RestaurantSearchFacade restaurantSearchFacade;

    @Override
    @PostMapping("/pickeats/{pickeatCode}/restaurants/location")
    public ResponseEntity<Void> createRestaurantsByLocation(
            @PathVariable("pickeatCode") String pickeatCode,
            @Valid @RequestBody LocationRestaurantRequest request
    ) {
        restaurantSearchFacade.searchByLocation(pickeatCode, request);
        URI location = URI.create("/pickeats/" + pickeatCode + "/restaurants");
        return ResponseEntity.created(location).build();
    }

    @Override
    @PostMapping("/pickeats/{pickeatCode}/restaurants/wish")
    public ResponseEntity<Void> createRestaurantsByWish(
            @PathVariable("pickeatCode") String pickeatCode,
            @Valid @RequestBody WishRestaurantRequest request
    ) {
        restaurantSearchFacade.searchByWish(pickeatCode, request);
        URI location = URI.create("/pickeats/" + pickeatCode + "/restaurants");
        return ResponseEntity.created(location).build();
    }

    @Override
    @PostMapping("/pickeats/{pickeatCode}/restaurants/template")
    public ResponseEntity<Void> createRestaurantsByTemplate(
            @PathVariable("pickeatCode") String pickeatCode,
            @Valid @RequestBody TemplateRestaurantRequest request
    ) {
        restaurantSearchFacade.searchByTemplate(pickeatCode, request);
        URI location = URI.create("/pickeats/" + pickeatCode + "/restaurants");
        return ResponseEntity.created(location).build();
    }

    @Override
    @GetMapping("/pickeats/restaurants")
    public ResponseEntity<List<RestaurantResponse>> getRestaurantMetaInPickeat(
            @Participant ParticipantPrincipal principal
    ) {
        List<RestaurantResponse> response = restaurantQueryService.getMetaInPickeat(principal.pickeatCode());
        return ResponseEntity.ok().body(response);
    }

    @Override
    @GetMapping("/pickeats/restaurants/state")
    public ResponseEntity<RestaurantStateResponse> getRestaurantStateInPickeat(
            @Participant ParticipantPrincipal principal
    ) {
        RestaurantStateResponse response = restaurantQueryService.getStateInPickeat(principal.pickeatCode());
        return ResponseEntity.ok().body(response);
    }

    @Override
    @PatchMapping("/restaurants/exclude")
    public ResponseEntity<Void> excludeRestaurants(
            @RequestBody RestaurantExcludeRequest request,
            @Participant ParticipantPrincipal principal
    ) {
        restaurantCommandService.exclude(principal.pickeatCode(), request.restaurantCodes());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/restaurants/{restaurantCode}/like")
    public ResponseEntity<Void> likeRestaurant(
            @PathVariable("restaurantCode") String restaurantCode,
            @Participant ParticipantPrincipal principal
    ) {
        restaurantCommandService.like(principal.pickeatCode(), principal.participantCode(), restaurantCode);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/restaurants/{restaurantCode}/unlike")
    public ResponseEntity<Void> cancelLikeRestaurant(
            @PathVariable("restaurantCode") String restaurantCode,
            @Participant ParticipantPrincipal principal
    ) {
        restaurantCommandService.cancelLike(principal.pickeatCode(), principal.participantCode(), restaurantCode);
        return ResponseEntity.noContent().build();
    }
}
