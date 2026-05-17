package com.pickeat.backend.restaurant.infrastructure.search.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.domain.FoodCategory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class KakaoRestaurantSearchParser {

    private static final Set<String> EXCLUDED_TAG_NAMES; // FoodCategory의 요소와 "음식점"은 태그명에서 제외

    static {
        EXCLUDED_TAG_NAMES = Stream
                .concat(FoodCategory.getNames().stream(), Stream.of("음식점"))
                .collect(Collectors.toUnmodifiableSet());
    }

    public List<RestaurantInfoDto> parsingResponse(JsonNode root) {
        return StreamSupport.stream(root.path("documents").spliterator(), false)
                .map(this::createRestaurantInfoDto)
                .toList();
    }

    private RestaurantInfoDto createRestaurantInfoDto(JsonNode document) {
        return RestaurantInfoDto.fromLocation(
                document.path("place_name").asText(),
                FoodCategory.parse(document.path("category_name").asText()),
                document.path("distance").asInt(),
                document.path("road_address_name").asText(),
                document.path("place_url").asText(),
                extractTags(document.path("category_name").asText())
        );
    }

    private String extractTags(String rawTags) {
        return Arrays.stream(rawTags.split(" > "))
                .filter(part -> !EXCLUDED_TAG_NAMES.contains(part))
                .collect(Collectors.joining(","));
    }
}
