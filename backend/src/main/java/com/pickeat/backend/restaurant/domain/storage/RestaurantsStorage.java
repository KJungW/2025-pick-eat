package com.pickeat.backend.restaurant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantsStorage {

    private static final DefaultRedisScript<Boolean> SETUP_RESTAURANTS_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 전체 식당 메타데이터 키 (String)
                    -- KEYS[2]: 생존한 식당 코드 목록 키 (Set)
                    -- ARGV[1]: TTL(초)
                    -- ARGV[2]: 전체 식당 메타데이터 JSON
                    -- ARGV[3...]: 제외 식당 코드들
                    
                    -- 이미 존재하는 데이터라면 실패 처리
                    if redis.call('EXISTS', KEYS[1]) == 1 or redis.call('EXISTS', KEYS[2]) == 1 then
                        return false
                    end
                    
                    -- "전체 식당 메타데이터" 저장 및 TTL 설정
                    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[1])
                    
                    -- "생존한 식당 코드 목록" 저장 및 TTL 설정
                    for i = 3, #ARGV do
                        redis.call('SADD', KEYS[2], ARGV[i])
                    end
                    redis.call('EXPIRE', KEYS[2], ARGV[1])
                    
                    -- 작업을 완료했으면 성공 처리
                    return true
                    """, Boolean.class);

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public Boolean setupRestaurants(String pickeatCode, RestaurantsV2 restaurants) {
        String restaurantMetaKey = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
        String restaurantAliveKey = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);

        Duration ttl = StorageKey.PICKEAT_TTL;
        Object[] args = makeSetupRestaurantsArgs(ttl, restaurants);

        Boolean result = redisTemplate.execute(
                SETUP_RESTAURANTS_SCRIPT,
                List.of(restaurantMetaKey, restaurantAliveKey),
                args
        );

        return Boolean.TRUE.equals(result);
    }

    public Optional<RestaurantsV2> getAllRestaurantMeta(String pickeatCode) {
        String key = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
        String jsonValue = redisTemplate.opsForValue().get(key);

        if (jsonValue == null) {
            return Optional.empty();
        }

        return Optional.of(jsonParser.fromJson(jsonValue, RestaurantsV2.class));
    }

    public void excludeRestaurants(String pickeatCode, List<String> restaurantCodes) {
        String key = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        redisTemplate.opsForSet().remove(key, restaurantCodes.toArray(Object[]::new));
    }

    public Set<String> getAliveRestaurantCode(String pickeatCode) {
        String key = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        return redisTemplate.opsForSet().members(key);
    }

    private Object[] makeSetupRestaurantsArgs(Duration ttl, RestaurantsV2 restaurants) {
        // 인자 구성: [0] TTL, [1] 전체 식당 정보 json, [2...] 전체 식당 코드들
        List<String> restaurantCodes = restaurants.extrudeRestaurantCodes();
        Object[] args = new Object[restaurantCodes.size() + 2];
        args[0] = String.valueOf(ttl.getSeconds());
        args[1] = jsonParser.toJson(restaurants);
        for (int i = 0; i < restaurantCodes.size(); i++) {
            args[i + 2] = restaurantCodes.get(i);
        }
        return args;
    }
}
