package com.pickeat.backend.restaurant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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
                    -- KEYS[2]: 생존 식당 코드 목록 키 (Set)
                    -- KEYS[3]: 식당별 좋아요 합계 키 (Hash)
                    
                    -- ARGV[1]: TTL(초)
                    -- ARGV[2]: 전체 식당 메타데이터 JSON
                    -- ARGV[3...]: 전체 식당 코드들
                    
                    -- 1. 이미 저장된 데이터면 실패 처리
                    if redis.call('EXISTS', KEYS[1]) == 1 then
                        return false
                    end
                    
                    -- 2. 식당 메타데이터 저장 및 TTL 설정
                    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[1])
                    
                    -- 3. 생존 식당 코드 목록 저장 / 식당별 좋아요수 저장
                    for i = 3, #ARGV do
                        redis.call('SADD', KEYS[2], ARGV[i])
                        redis.call('HSET', KEYS[3], ARGV[i], 0)
                    end
                    
                    -- 4. 생존 식당 코드 목록 TTL 설정 / 식당별 좋아요수 TTL 설정
                    redis.call('EXPIRE', KEYS[2], ARGV[1])
                    redis.call('EXPIRE', KEYS[3], ARGV[1])
                    
                    return true
                    """, Boolean.class);

    private static final DefaultRedisScript<List> GET_RESTAURANT_STATE_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 생존 식당 코드 목록 키 (Set)
                    -- KEYS[2]: 식당별 좋아요 합계 키 (Hash)
                    
                    local alive_codes = redis.call('SMEMBERS', KEYS[1])
                    local like_counts = redis.call('HGETALL', KEYS[2])
                    
                    return {alive_codes, like_counts}
                    """, List.class);

    private static final DefaultRedisScript<Boolean> LIKE_RESTAURANT_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 식당별 참가자의 좋아요 기록 키 (Set)
                    -- KEYS[2]: 식당별 좋아요 합계 키 (Hash)
                    
                    -- ARGV[1]: TTL (초)
                    -- ARGV[2]: 참가자 코드
                    -- ARGV[3]: 식당 코드
                    
                    -- 1. 좋아요 기록에 참가자 추가 시도 (SADD는 새로 추가될 때만 1을 반환함)
                    if redis.call('SADD', KEYS[1], ARGV[2]) == 1 then
                        -- 2. 신규 좋아요라면 전체 합계에서 해당 식당의 count를 1 증가
                        redis.call('HINCRBY', KEYS[2], ARGV[3], 1)
                    
                        -- 3. 좋아요 기록 키에 TTL 설정 (최초 생성 시점에만 걸리도록 TTL 체크 후 설정)
                        if redis.call('TTL', KEYS[1]) < 0 then
                            redis.call('EXPIRE', KEYS[1], ARGV[1])
                        end
                        return true
                    end
                    
                    -- 이미 좋아요를 누른 기록이 있다면 false 반환
                    return false
                    """, Boolean.class);

    private static final DefaultRedisScript<Boolean> CANCEL_LIKE_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 식당별 참가자의 좋아요 기록 키 (Set)
                    -- KEYS[2]: 식당별 좋아요 합계 키 (Hash)
                    
                    -- ARGV[1]: 참가자 코드
                    -- ARGV[2]: 식당 코드
                    
                    -- 1. 좋아료 기록에서 참가자 제거 시도 (SREM은 데이터가 있어 제거 성공 시 1을 반환함)
                    if redis.call('SREM', KEYS[1], ARGV[1]) == 1 then
                        -- 2. 제거 성공 시(좋아요를 눌렀던 상태), 전체 합계(Hash)에서 1 감소
                        redis.call('HINCRBY', KEYS[2], ARGV[2], -1)
                        return true
                    end
                    
                    -- 원래 좋아요를 누르지 않았던 상태라면 false 반환
                    return false
                    """, Boolean.class);

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public boolean setupRestaurants(String pickeatCode, RestaurantsV2 restaurants) {
        String restaurantMetaKey = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
        String restaurantAliveKey = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        String restaurantLikeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        Duration ttl = StorageKey.PICKEAT_TTL;
        Object[] args = makeSetupRestaurantsArgs(ttl, restaurants);

        Boolean result = redisTemplate.execute(
                SETUP_RESTAURANTS_SCRIPT,
                List.of(restaurantMetaKey, restaurantAliveKey, restaurantLikeCountKey),
                args);
        return Boolean.TRUE.equals(result);
    }

    public Optional<RestaurantsV2> getAllRestaurantMeta(String pickeatCode) {
        String key = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
        String result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(jsonParser.fromJson(result, RestaurantsV2.class));
    }

    public RestaurantStateDto getAllRestaurantState(String pickeatCode) {
        String aliveKey = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        List<Object> result = redisTemplate.execute(GET_RESTAURANT_STATE_SCRIPT, List.of(aliveKey, likeCountKey));
        return parseGetAllRestaurantStateResult(result);
    }

    public void excludeRestaurants(String pickeatCode, List<String> restaurantCodes) {
        String key = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
        redisTemplate.opsForSet().remove(key, restaurantCodes.toArray(Object[]::new));
    }

    public boolean like(String pickeatCode, String participantCode, String restaurantCode) {
        String likeRecordKey = StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, restaurantCode);
        String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
        Duration ttl = StorageKey.PICKEAT_TTL;

        Boolean isSuccess = redisTemplate.execute(
                LIKE_RESTAURANT_SCRIPT,
                List.of(likeRecordKey, likeCountKey),
                String.valueOf(ttl.getSeconds()),
                participantCode,
                restaurantCode);
        return Boolean.TRUE.equals(isSuccess);
    }

    public Boolean cancelLike(String pickeatCode, String participantCode, String restaurantCode) {
        String likeRecordKey = StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, restaurantCode);
        String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);

        Boolean isSuccess = redisTemplate.execute(
                CANCEL_LIKE_SCRIPT,
                List.of(likeRecordKey, likeCountKey),
                participantCode,
                restaurantCode);
        return Boolean.TRUE.equals(isSuccess);
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

    private RestaurantStateDto parseGetAllRestaurantStateResult(List<Object> result) {
        if (result == null || result.isEmpty()) {
            return new RestaurantStateDto(Set.of(), Map.of());
        }
        Set<String> aliveCodes = Set.copyOf((List<String>) result.get(0));
        List<String> likeCountFlatHash = (List<String>) result.get(1);
        Map<String, Integer> map = new java.util.HashMap<>();
        for (int i = 0; i < likeCountFlatHash.size(); i += 2) {
            map.put(likeCountFlatHash.get(i), Integer.parseInt(likeCountFlatHash.get(i + 1)));
        }
        return new RestaurantStateDto(aliveCodes, map);
    }
}
