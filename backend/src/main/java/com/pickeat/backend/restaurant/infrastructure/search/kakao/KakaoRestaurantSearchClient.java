package com.pickeat.backend.restaurant.infrastructure.search.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickeat.backend.global.exception.code.ExternalErrorCode;
import com.pickeat.backend.global.exception.code.ServerErrorCode;
import com.pickeat.backend.global.exception.type.ExternalException;
import com.pickeat.backend.global.exception.type.ServerException;
import com.pickeat.backend.restaurant.application.client.RestaurantSearchClient;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.external.RestaurantSearchClientRequest;
import io.github.bucket4j.Bucket;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
public class KakaoRestaurantSearchClient implements RestaurantSearchClient {

    private static final String PLATFORM_NAME = "kakao";                // 플랫폼 이름
    private static final String URI = "v2/local/search/keyword.json";   // 카카오맵 조회 API URI
    private static final String SORT = "accuracy";                      // 정렬 기준 (distance: 거리, accuracy: 정확도)
    private static final String CATEGORY_GROUP_CODE = "FD6";            // 카카오맵에서 식당을 나타내는 코드
    private static final long BUCKET_NANO_TIMEOUT = Duration.ofSeconds(2).toNanos();

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final KakaoRestaurantSearchParser parser;
    private final Bucket restaurantSearchBucket;

    public List<RestaurantInfoDto> getRestaurants(RestaurantSearchClientRequest request) {
        try {
            boolean consume = restaurantSearchBucket.asBlocking().tryConsume(1, BUCKET_NANO_TIMEOUT);
            if (!consume) {
                throw new ExternalException(ExternalErrorCode.RATE_LIMIT_WAIT_TIMEOUT, PLATFORM_NAME);
            }
            return requestExternalApi(request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalException(ExternalErrorCode.THREAD_TIMEOUT, PLATFORM_NAME, e);

        } catch (RestClientException e) {
            throw new ExternalException(ExternalErrorCode.EXTERNAL_API_TIMEOUT, PLATFORM_NAME, e);

        }
    }

    private List<RestaurantInfoDto> requestExternalApi(RestaurantSearchClientRequest searchRequest) {
        JsonNode root = restClient.get().uri(uriBuilder -> uriBuilder
                        .path(URI)
                        .queryParam("category", searchRequest.category())
                        .queryParam("category_group_code", CATEGORY_GROUP_CODE)
                        .queryParam("x", searchRequest.x())
                        .queryParam("y", searchRequest.y())
                        .queryParam("radius", searchRequest.radius())
                        .queryParam("size", searchRequest.size())
                        .queryParam("sort", SORT)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> handleError(response))
                .body(JsonNode.class);
        return parser.parsingResponse(root);
    }

    private void handleError(ClientHttpResponse response) {
        try {
            JsonNode errorRoot = objectMapper.readTree(response.getBody());
            String errorBody = objectMapper.writeValueAsString(errorRoot);
            int errorStatus = response.getStatusCode().value();

            String errorMessage = String.format("%d : %s", errorStatus, errorBody);
            throw new ExternalException(ExternalErrorCode.EXTERNAL_API_FAIL, PLATFORM_NAME, errorMessage);
        } catch (IOException e) {
            throw new ServerException(ServerErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
