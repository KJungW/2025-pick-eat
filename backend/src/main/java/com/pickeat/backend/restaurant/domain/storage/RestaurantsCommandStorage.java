package com.pickeat.backend.restaurant.domain.storage;

import static com.pickeat.backend.restaurant.domain.storage.RestaurantStorageScript.CANCEL_LIKE_SCRIPT;
import static com.pickeat.backend.restaurant.domain.storage.RestaurantStorageScript.LIKE_RESTAURANT_SCRIPT;
import static com.pickeat.backend.restaurant.domain.storage.RestaurantStorageScript.SETUP_RESTAURANTS_SCRIPT;

import com.pickeat.backend.global.configuration.storage.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.domain.Restaurants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantsCommandStorage {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public Boolean setupRestaurants(String pickeatCode, Restaurants restaurants) {
        List<String> keys = List.of(
                StorageKey.RESTAURANT_META.generateKey(pickeatCode),
                StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode),
                StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode),
                StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode)
        );
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(StorageKey.PICKEAT_TTL.getSeconds()));
        args.add(jsonParser.toJson(restaurants));
        args.addAll(restaurants.extrudeRestaurantCodes());

        return redisTemplate.execute(SETUP_RESTAURANTS_SCRIPT, keys, args);
    }

    public void excludeRestaurants(String pickeatCode, List<String> restaurantCodes) {
        String key = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        redisTemplate.opsForSet().remove(key, restaurantCodes.toArray(Object[]::new));
    }

    public Boolean like(String pickeatCode, String participantCode, String restaurantCode) {
        List<String> keys = List.of(
                StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, restaurantCode),
                StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode)
        );
        List<String> args = List.of(
                String.valueOf(StorageKey.PICKEAT_TTL.getSeconds()),
                participantCode,
                restaurantCode
        );

        return redisTemplate.execute(LIKE_RESTAURANT_SCRIPT, keys, args);
    }

    public Boolean cancelLike(String pickeatCode, String participantCode, String restaurantCode) {
        List<String> keys = List.of(
                StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, restaurantCode),
                StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode)
        );
        List<String> args = List.of(
                participantCode,
                restaurantCode
        );

        return redisTemplate.execute(CANCEL_LIKE_SCRIPT, keys, args);
    }

    public void remove(String pickeatCode) {
        List<String> allKey = new java.util.ArrayList<>();
        allKey.add(StorageKey.RESTAURANT_META.generateKey(pickeatCode));
        allKey.add(StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode));
        allKey.add(StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode));
        allKey.addAll(getAllRestaurantLikeRecordKey(pickeatCode));
        allKey.add(StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode));
        redisTemplate.delete(allKey);
    }

    private List<String> getAllRestaurantLikeRecordKey(String pickeatCode) {
        String restaurantLikeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        Set<Object> restaurantCodes = redisTemplate.opsForHash().keys(restaurantLikeCountKey);
        return restaurantCodes.stream()
                .map(code -> StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, code))
                .toList();
    }
}
