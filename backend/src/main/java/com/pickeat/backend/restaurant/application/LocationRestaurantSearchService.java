package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.code.ServerErrorCode;
import com.pickeat.backend.global.exception.type.BaseException;
import com.pickeat.backend.global.exception.type.ServerException;
import com.pickeat.backend.restaurant.application.client.RestaurantSearchClient;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.external.RestaurantSearchClientRequest;
import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
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
    private static final int THREAD_TIMEOUT = 5;
    private static final List<String> CATEGORIES = List.of("한식", "양식", "중식", "일식", "아시안음식");

    private final RestaurantSearchClient restaurantSearchClient;
    private final TaskExecutor virtualThreadExecutor;

    public List<RestaurantInfoDto> searchByLocation(LocationRestaurantRequest request) {
        List<RestaurantSearchClientRequest> clientRequests = createClientRequest(request);
        List<CompletableFuture<List<RestaurantInfoDto>>> futures = createClientRequestFutures(clientRequests);
        return executeAllFuture(futures);
    }

    private List<RestaurantSearchClientRequest> createClientRequest(LocationRestaurantRequest request) {
        return CATEGORIES.stream()
                .map(category -> RestaurantSearchClientRequest.of(request, category, RESTAURANT_SEARCH_SIZE))
                .toList();
    }

    private List<CompletableFuture<List<RestaurantInfoDto>>> createClientRequestFutures(
            List<RestaurantSearchClientRequest> clientRequests
    ) {
        return clientRequests.stream()
                .map(request -> CompletableFuture
                        .supplyAsync(() -> restaurantSearchClient.getRestaurants(request), virtualThreadExecutor)
                        .orTimeout(THREAD_TIMEOUT, TimeUnit.SECONDS))
                .toList();
    }

    private List<RestaurantInfoDto> executeAllFuture(
            List<CompletableFuture<List<RestaurantInfoDto>>> futures
    ) {
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .toList();

        } catch (CompletionException e) {
            if (e.getCause() instanceof BaseException exception) {
                throw exception;
            }
            throw new ServerException(ServerErrorCode.INTERNAL_SERVER_ERROR, e.getCause());
        }
    }
}
