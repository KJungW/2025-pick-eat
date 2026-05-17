package com.pickeat.backend.pickeat.domain.store;

import com.pickeat.backend.global.configuration.storage.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.pickeat.domain.Pickeat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickeatStorage {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public void save(Pickeat pickeat) {
        String key = StorageKey.PICKEAT.generateKey(pickeat.getCode());
        String value = jsonParser.toJson(pickeat);
        redisTemplate.opsForValue().set(key, value, StorageKey.PICKEAT_TTL);
    }

    public Optional<Pickeat> getMeta(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        String result = redisTemplate.opsForValue().get(key);
        return parsePickeatMeta(result);
    }

    public void remove(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        redisTemplate.delete(key);
    }

    private Optional<Pickeat> parsePickeatMeta(String result) {
        if (result == null) {
            return Optional.empty();
        }

        Pickeat pickeat = jsonParser.fromJson(result, Pickeat.class);
        return Optional.ofNullable(pickeat);
    }
}
