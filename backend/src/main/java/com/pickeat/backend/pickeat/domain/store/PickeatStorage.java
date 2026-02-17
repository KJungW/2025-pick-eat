package com.pickeat.backend.pickeat.domain.store;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.pickeat.domain.Pickeat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickeatStorage {

    private final StringRedisTemplate template;
    private final JsonParser jsonParser;

    public void save(Pickeat pickeat) {
        String key = StorageKey.PICKEAT.generateKey(pickeat.getCode());
        String jsonValue = jsonParser.toJson(pickeat);
        template.opsForValue().set(key, jsonValue, StorageKey.PICKEAT_TTL);
    }

    public Optional<Pickeat> get(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        String result = template.opsForValue().get(key);

        if (result == null) {
            return Optional.empty();
        }

        Pickeat pickeat = jsonParser.fromJson(result, Pickeat.class);
        return Optional.ofNullable(pickeat);
    }

    public void remove(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        template.delete(key);
    }
}
