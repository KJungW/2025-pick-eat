package com.pickeat.backend.restaurant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.domain.Restaurants;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantsStorage {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public Boolean setupRestaurants(String pickeatCode, Restaurants restaurants) {
        String restaurantMetaKey = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
        String restaurantAliveKey = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        String restaurantLikeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        Duration ttl = StorageKey.PICKEAT_TTL;
        Object[] args = makeSetupRestaurantsArgs(ttl, restaurants);

        return redisTemplate.execute(
                RestaurantStorageScript.SETUP_RESTAURANTS_SCRIPT,
                List.of(restaurantMetaKey, restaurantAliveKey, restaurantLikeCountKey),
                args);
    }

    public Optional<Restaurants> getRestaurantMeta(String pickeatCode) {
        String key = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
        String result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(jsonParser.fromJson(result, Restaurants.class));
    }

    public Optional<RestaurantStateDto> getRestaurantState(String pickeatCode) {
        String aliveKey = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        String sequenceKey = StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode);

        List<Object> result = redisTemplate.execute(
                RestaurantStorageScript.GET_STATE_SCRIPT,
                List.of(aliveKey, likeCountKey, sequenceKey)
        );
        return parseGetAllRestaurantState(result);
    }

    public Optional<RestaurantStateDto> getRestaurantStateWithSequence(String pickeatCode) {
        String aliveKey = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        String sequenceKey = StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode);
        Duration ttl = StorageKey.PICKEAT_TTL;

        List<Object> result = redisTemplate.execute(
                RestaurantStorageScript.GET_STATE_AND_INCR_SEQUENCE_SCRIPT,
                List.of(aliveKey, likeCountKey, sequenceKey),
                String.valueOf(ttl.getSeconds())
        );
        return parseGetAllRestaurantState(result);
    }

    public void excludeRestaurants(String pickeatCode, List<String> restaurantCodes) {
        String key = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        redisTemplate.opsForSet().remove(key, restaurantCodes.toArray(Object[]::new));
    }

    public Boolean like(String pickeatCode, String participantCode, String restaurantCode) {
        String likeRecordKey = StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, restaurantCode);
        String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        Duration ttl = StorageKey.PICKEAT_TTL;

        return redisTemplate.execute(
                RestaurantStorageScript.LIKE_RESTAURANT_SCRIPT,
                List.of(likeRecordKey, likeCountKey),
                String.valueOf(ttl.getSeconds()),
                participantCode,
                restaurantCode);
    }

    public Boolean cancelLike(String pickeatCode, String participantCode, String restaurantCode) {
        String likeRecordKey = StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, restaurantCode);
        String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);

        return redisTemplate.execute(
                RestaurantStorageScript.CANCEL_LIKE_SCRIPT,
                List.of(likeRecordKey, likeCountKey),
                participantCode,
                restaurantCode);
    }

    public void remove(String pickeatCode) {
        List<String> allRestaurantKeyInPickeat = getAllRestaurantKeyInPickeat(pickeatCode);
        redisTemplate.delete(allRestaurantKeyInPickeat);
    }

    private Object[] makeSetupRestaurantsArgs(Duration ttl, Restaurants restaurants) {
        List<String> restaurantCodes = restaurants.extrudeRestaurantCodes();
        Object[] args = new Object[restaurantCodes.size() + 2];
        args[0] = String.valueOf(ttl.getSeconds());
        args[1] = jsonParser.toJson(restaurants);
        for (int i = 0; i < restaurantCodes.size(); i++) {
            args[i + 2] = restaurantCodes.get(i);
        }
        return args;
    }

    private Optional<RestaurantStateDto> parseGetAllRestaurantState(List<Object> result) {
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

    private List<String> getAllRestaurantKeyInPickeat(String pickeatCode) {
        List<String> allKey = new java.util.ArrayList<>();
        allKey.add(StorageKey.RESTAURANT_META.generateKey(pickeatCode));
        allKey.add(StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode));
        allKey.add(StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode));
        allKey.addAll(getAllRestaurantLikeRecordKey(pickeatCode));
        return allKey;
    }

    private List<String> getAllRestaurantLikeRecordKey(String pickeatCode) {
        String restaurantLikeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        Set<Object> restaurantCodes = redisTemplate.opsForHash().keys(restaurantLikeCountKey);
        return restaurantCodes.stream()
                .map(code -> StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, code))
                .toList();
    }
}
