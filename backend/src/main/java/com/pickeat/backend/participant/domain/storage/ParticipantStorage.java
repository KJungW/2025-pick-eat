package com.pickeat.backend.participant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import com.pickeat.backend.participant.domain.Participant;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantStorage {

    private static final DefaultRedisScript<Boolean> ADD_PARTICIPANT_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 참가자 리스트 키 (LIST)
                    -- KEYS[2]: 참가자 완료 여부 키 (HASH)
                    -- ARGV[1]: TTL (초 단위)
                    -- ARGV[2]: 참가자 JSON 데이터
                    -- ARGV[3]: 참가자 코드 (HASH의 필드로 사용)
                    
                    -- 1. 참가자 리스트 : 리스트의 오른쪽에 참가자 추가 (RPUSH)
                    redis.call('RPUSH', KEYS[1], ARGV[2])
                    
                    -- 2. 참가자 리스트 : 처음 리스트가 생성될 때에 한해 TTL 설정
                    if redis.call('TTL', KEYS[1]) < 0 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end
                    
                    -- 3. 참가자 완료 여부 해시 : 참가자 - 완료 여부 초기값(false) 저장
                    redis.call('HSET', KEYS[2], ARGV[3], 'false')
                    
                    -- 4. 참가자 완료 여부 해시 : 처음 해시가 생성될 때에 한해 TTL 설정
                    if redis.call('TTL', KEYS[2]) < 0 then
                        redis.call('EXPIRE', KEYS[2], ARGV[1])
                    end
                    
                    return true
                    """, Boolean.class);


    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public Boolean setupAboutParticipant(String pickeatCode, Participant participant) {
        String participantKey = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        String participantCompletionKey = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
        String participantValue = jsonParser.toJson(participant);
        String participantCode = participant.getCode();
        Duration ttl = StorageKey.PICKEAT_TTL;

        return redisTemplate.execute(
                ADD_PARTICIPANT_SCRIPT,
                List.of(participantKey, participantCompletionKey),
                String.valueOf(ttl.getSeconds()),
                participantValue,
                participantCode);
    }

    public List<Participant> getParticipantsMeta(String pickeatCode) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        List<String> rawParticipants = redisTemplate.opsForList().range(key, 0, -1);

        if (rawParticipants == null || rawParticipants.isEmpty()) {
            return List.of();
        }

        return rawParticipants.stream()
                .map(json -> jsonParser.fromJson(json, Participant.class))
                .toList();
    }

    public Optional<ParticipantStateDto> getParticipantsState(String pickeatCode) {
        String key = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
        Map<Object, Object> result = redisTemplate.opsForHash().entries(key);
        return parseParticipantState(result);
    }

    public void markCompletion(String pickeatCode, String participantCode) {
        String key = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
        redisTemplate.opsForHash().put(key, participantCode, "true");
    }

    public void cancelCompletion(String pickeatCode, String participantCode) {
        String key = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
        redisTemplate.opsForHash().put(key, participantCode, "false");
    }

    public void remove(String pickeatCode) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        redisTemplate.delete(key);
    }

    private Optional<ParticipantStateDto> parseParticipantState(Map<Object, Object> result) {
        if (result.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Boolean> completionState = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : result.entrySet()) {
            String participantCode = (String) entry.getKey();
            Boolean isComplete = Boolean.parseBoolean((String) entry.getValue());
            completionState.put(participantCode, isComplete);
        }
        return Optional.of(new ParticipantStateDto(completionState));
    }
}
