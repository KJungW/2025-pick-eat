package com.pickeat.backend.pickeat.domain.store;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.pickeat.domain.Pickeat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickeatStorage {

    private final RedisTemplate<String, Object> template;

    public void save(Pickeat pickeat) {
        String key = StorageKey.PICKEAT.generateKey(pickeat.getCode());
        template.opsForValue().set(key, pickeat, StorageKey.PICKEAT.getTtl());
    }

    public Optional<Pickeat> get(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        Pickeat result = (Pickeat) template.opsForValue().get(key);
        return Optional.ofNullable(result);
    }

    public void remove(String pickeatCode) {
        String key = StorageKey.PICKEAT.generateKey(pickeatCode);
        template.delete(key);
    }
}
