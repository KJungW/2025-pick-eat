package com.pickeat.backend.restaurant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantsStorage {

    private final StringRedisTemplate stringTemplate;
    private final JsonParser jsonParser;

    public Boolean saveIfAbsent(RestaurantsV2 restaurants, String pickeatCode) {
        String key = StorageKey.RESTAURANT.generateKey(pickeatCode);
        return stringTemplate.opsForValue()
                .setIfAbsent(key, jsonParser.toJson(restaurants), StorageKey.RESTAURANT.getTtl());
    }

    public Optional<RestaurantsV2> get(String pickeatCode) {
        String key = StorageKey.RESTAURANT.generateKey(pickeatCode);
        String jsonValue = stringTemplate.opsForValue().get(key);

        if (jsonValue == null) {
            return Optional.empty();
        }

        return Optional.of(jsonParser.fromJson(jsonValue, RestaurantsV2.class));
    }
}
