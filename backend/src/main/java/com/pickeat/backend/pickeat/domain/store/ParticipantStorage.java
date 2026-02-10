package com.pickeat.backend.pickeat.domain.store;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.pickeat.domain.ParticipantV2;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantStorage {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public void save(String pickeatCode, ParticipantV2 participant) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        String field = participant.getCode();
        String value = jsonParser.toJson(participant);
        long ttlSeconds = StorageKey.PARTICIPANT.getTtl().getSeconds();

        String script = """
                redis.call('HSET', KEYS[1], ARGV[1], ARGV[2]);
                if redis.call('TTL', KEYS[1]) < 0 then
                    redis.call('EXPIRE', KEYS[1], ARGV[3]);
                end
                return 1;
                """;

        redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                List.of(key),
                field,
                value,
                String.valueOf(ttlSeconds)
        );
    }

    public Optional<ParticipantV2> get(String pickeatCode, String participantCode) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        Object value = redisTemplate.opsForHash().get(key, participantCode);

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(jsonParser.fromJson((String) value, ParticipantV2.class));
    }
}
