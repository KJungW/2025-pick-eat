package com.pickeat.backend.participant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.domain.ParticipantV2;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantStorage {

    private static final DefaultRedisScript<Void> ADD_PARTICIPANT_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 참가자 리스트 키
                    -- ARGV[1]: TTL (초 단위)
                    -- ARGV[2]: 참가자 JSON 데이터
                    
                    -- 리스트의 오른쪽에 데이터 추가 (RPUSH)
                    redis.call('RPUSH', KEYS[1], ARGV[2])
                    
                    -- 해당 키에 만료 시간 설정
                    if redis.call('TTL', KEYS[1]) < 0 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end
                    """, Void.class);


    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public void setupAboutParticipant(String pickeatCode, ParticipantV2 participant) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        String value = jsonParser.toJson(participant);
        Duration ttl = StorageKey.PICKEAT_TTL;

        redisTemplate.execute(
                ADD_PARTICIPANT_SCRIPT,
                List.of(key),
                String.valueOf(ttl.getSeconds()),
                value);
    }

    public List<ParticipantV2> getParticipants(String pickeatCode) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        List<String> rawParticipants = redisTemplate.opsForList().range(key, 0, -1);

        if (rawParticipants == null || rawParticipants.isEmpty()) {
            return List.of();
        }

        return rawParticipants.stream()
                .map(json -> jsonParser.fromJson(json, ParticipantV2.class))
                .toList();
    }

    public void remove(String pickeatCode) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        redisTemplate.delete(key);
    }
}
