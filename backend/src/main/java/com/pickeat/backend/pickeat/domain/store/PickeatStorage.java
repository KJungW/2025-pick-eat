package com.pickeat.backend.pickeat.domain.store;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickeatStorage {

    private final RedisTemplate<String, Object> template;

    public void save(PickeatV2 pickeat) {
        String key = StorageKey.PICKEAT.generateKey(pickeat.getCode());
        template.opsForValue().set(key, pickeat, StorageKey.PICKEAT.getTtl());
    }

    public Optional<PickeatV2> get(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        PickeatV2 result = (PickeatV2) template.opsForValue().get(key);
        return Optional.ofNullable(result);
    }

    public void remove(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        template.delete(key);
    }
}
