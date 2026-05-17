package com.pickeat.backend.participant.domain.storage;

import static com.pickeat.backend.participant.domain.storage.ParticipantStorageScript.ADD_PARTICIPANT_SCRIPT;
import static com.pickeat.backend.participant.domain.storage.ParticipantStorageScript.GET_ALL_STATE_AND_INCR_SEQUENCE_SCRIPT;
import static com.pickeat.backend.participant.domain.storage.ParticipantStorageScript.GET_ALL_STATE_SCRIPT;

import com.pickeat.backend.global.configuration.storage.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import com.pickeat.backend.participant.domain.Participant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantStorage {

    private final StringRedisTemplate redisTemplate;
    private final JsonParser jsonParser;

    public Boolean saveAboutParticipant(String pickeatCode, Participant participant) {
        List<String> keys = List.of(
                StorageKey.PARTICIPANT.generateKey(pickeatCode),
                StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode)
        );
        Object[] args = new Object[]{
                String.valueOf(StorageKey.PICKEAT_TTL.getSeconds()),
                jsonParser.toJson(participant),
                participant.getCode()
        };
        return redisTemplate.execute(ADD_PARTICIPANT_SCRIPT, keys, args);
    }

    public List<Participant> getParticipantsMeta(String pickeatCode) {
        String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        List<String> rawParticipants = redisTemplate.opsForList().range(key, 0, -1);
        return parseParticipantMeta(rawParticipants);
    }

    public Optional<ParticipantStateDto> getParticipantsState(String pickeatCode) {
        List<String> keys = List.of(
                StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode),
                StorageKey.PARTICIPANT_SEQUENCE.generateKey(pickeatCode)
        );
        Object[] args = new Object[]{
                String.valueOf(StorageKey.PICKEAT_TTL.getSeconds())
        };

        List<Object> results = redisTemplate.execute(GET_ALL_STATE_SCRIPT, keys, args);
        return parseParticipantState(results);
    }

    public Optional<ParticipantStateDto> getParticipantsStateWithSequence(String pickeatCode) {
        List<String> keys = List.of(
                StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode),
                StorageKey.PARTICIPANT_SEQUENCE.generateKey(pickeatCode)
        );
        Object[] args = new Object[]{
                String.valueOf(StorageKey.PICKEAT_TTL.toSeconds())
        };

        List<Object> results = redisTemplate.execute(GET_ALL_STATE_AND_INCR_SEQUENCE_SCRIPT, keys, args);
        return parseParticipantState(results);
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
        List<String> allKey = new java.util.ArrayList<>();
        allKey.add(StorageKey.PARTICIPANT.generateKey(pickeatCode));
        allKey.add(StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode));
        allKey.add(StorageKey.PARTICIPANT_SEQUENCE.generateKey(pickeatCode));
        redisTemplate.delete(allKey);
    }

    private List<Participant> parseParticipantMeta(List<String> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(json -> jsonParser.fromJson(json, Participant.class))
                .toList();
    }


    private Optional<ParticipantStateDto> parseParticipantState(List<Object> results) {
        // results 구조 : { 참가자 상태 시퀀스 값 (String), 모든 참가자의 투표 완료 여부 (Hash:참가자코드-완료여부) }

        // 응답이 제대로 구성되어있는지 체크
        if (results == null || results.size() < 2) {
            return Optional.empty();
        }

        // 모든 참가자의 투표 완료 여부 (Hash:참가자코드-완료여부) 파싱
        List<Object> rawData = (List<Object>) results.get(1);
        if (rawData == null || rawData.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Boolean> dataMap = new HashMap<>();
        for (int i = 0; i < rawData.size(); i += 2) {
            dataMap.put(String.valueOf(rawData.get(i)), Boolean.valueOf(String.valueOf(rawData.get(i + 1))));
        }

        // 참가자 상태 시퀀스 값 (String) 파싱
        Long sequence = Long.parseLong((String) results.get(0));

        return Optional.of(new ParticipantStateDto(sequence, dataMap));
    }
}
