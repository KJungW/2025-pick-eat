package com.pickeat.backend.restaurant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantExcludedStorage {

    private static final DefaultRedisScript<Long> SETUP_EXCLUDE_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('EXISTS', KEYS[1]) == 1 then
                        return 0;
                    end
                    
                    for i=2, #ARGV do
                        redis.call('SADD', KEYS[1], ARGV[i]);
                    end
                    
                    redis.call('EXPIRE', KEYS[1], ARGV[1]);
                    
                    return 1;
                    """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public boolean setupExclude(String pickeatCode, List<String> restaurantCodes) {
        String key = StorageKey.RESTAURANT_EXCLUDED.generateKey(pickeatCode);
        Duration ttl = StorageKey.RESTAURANT_EXCLUDED.getTtl();

        Long result = redisTemplate.execute(
                SETUP_EXCLUDE_SCRIPT,
                List.of(key),
                setupCreateArgs(ttl, restaurantCodes));

        return Long.valueOf(1).equals(result);
    }

    public void exclude(String pickeatCode, List<String> restaurantCodes) {
        String key = StorageKey.RESTAURANT_EXCLUDED.generateKey(pickeatCode);
        redisTemplate.opsForSet().remove(key, restaurantCodes.toArray(Object[]::new));
    }

    public Set<String> findAlive(String pickeatCode) {
        String key = StorageKey.RESTAURANT_EXCLUDED.generateKey(pickeatCode);
        return redisTemplate.opsForSet().members(key);
    }

    private Object[] setupCreateArgs(Duration ttl, List<String> restaurantCodes) {
        Object[] args = new Object[restaurantCodes.size() + 1];
        args[0] = String.valueOf(ttl.getSeconds());
        for (int i = 0; i < restaurantCodes.size(); i++) {
            args[i + 1] = restaurantCodes.get(i);
        }
        return args;
    }
}
