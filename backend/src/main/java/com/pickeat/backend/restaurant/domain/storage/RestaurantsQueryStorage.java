package com.pickeat.backend.restaurant.domain.storage;

import static com.pickeat.backend.restaurant.domain.storage.RestaurantStorageScript.GET_STATE_AND_INCR_SEQUENCE_SCRIPT;
import static com.pickeat.backend.restaurant.domain.storage.RestaurantStorageScript.GET_STATE_SCRIPT;

import com.pickeat.backend.global.configuration.storage.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.domain.Restaurants;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantsQueryStorage {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public Optional<Restaurants> getRestaurantMeta(String pickeatCode) {
        String key = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
        String result = redisTemplate.opsForValue().get(key);
        return parseRestaurantMeta(result);
    }

    public Optional<RestaurantStateDto> getRestaurantState(String pickeatCode) {
        List<String> keys = List.of(
                StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode),
                StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode),
                StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode)
        );

        List<Object> result = redisTemplate.execute(GET_STATE_SCRIPT, keys);
        return parseRestaurantState(result);
    }

    public Optional<RestaurantStateDto> getRestaurantStateWithSequence(String pickeatCode) {
        List<String> keys = List.of(
                StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode),
                StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode),
                StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode)
        );
        List<String> args = List.of(
                String.valueOf(StorageKey.PICKEAT_TTL.getSeconds())
        );

        List<Object> result = redisTemplate.execute(GET_STATE_AND_INCR_SEQUENCE_SCRIPT, keys, args);
        return parseRestaurantState(result);
    }

    private Optional<Restaurants> parseRestaurantMeta(String result) {
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(jsonParser.fromJson(result, Restaurants.class));
    }

    private Optional<RestaurantStateDto> parseRestaurantState(List<Object> result) {
        if (result == null || result.size() < 3) {
            return Optional.empty();
        }

        // 시퀀스 번호 파싱
        Long sequence = Long.parseLong(String.valueOf(result.get(0)));

        // 생존 식당 코드 목록 파싱
        Set<String> aliveCodes = Set.copyOf((List<String>) result.get(1));

        // 식당별 좋아요수 파싱
        List<String> likeCountFlatHash = (List<String>) result.get(2);
        if (likeCountFlatHash.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Integer> likeCountMap = new java.util.HashMap<>();
        for (int i = 0; i < likeCountFlatHash.size(); i += 2) {
            String code = likeCountFlatHash.get(i);
            String count = likeCountFlatHash.get(i + 1);
            likeCountMap.put(code, Integer.parseInt(count));
        }

        return Optional.of(new RestaurantStateDto(sequence, aliveCodes, likeCountMap));
    }
}
