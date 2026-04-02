package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantSearchRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationRestaurantSearchService {

    private static final int RESTAURANT_SEARCH_SIZE = 10;
    private static final List<String> CATEGORIES = List.of("한식", "양식", "중식", "일식", "아시안음식");

    private final RestaurantSearchClient restaurantSearchClient;
    private final TaskExecutor virtualThreadExecutor;

    public List<RestaurantRequest> searchByLocation(LocationRestaurantRequest request) {
        List<CompletableFuture<List<RestaurantRequest>>> futures = makeGetRestaurantFuture(request);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join();
            return futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw e;
        }
    }

    private List<CompletableFuture<List<RestaurantRequest>>> makeGetRestaurantFuture(
            LocationRestaurantRequest request
    ) {
        return CATEGORIES.stream()
                .map(category -> CompletableFuture
                        .supplyAsync(() -> getRestaurants(category, request), virtualThreadExecutor)
                        .orTimeout(5, TimeUnit.SECONDS)
                )
                .toList();
    }

    private List<RestaurantRequest> getRestaurants(String category, LocationRestaurantRequest request) {
        return restaurantSearchClient.getRestaurants(
                new RestaurantSearchRequest(category, request.x(), request.y(), request.radius(),
                        RESTAURANT_SEARCH_SIZE));
    }
}
