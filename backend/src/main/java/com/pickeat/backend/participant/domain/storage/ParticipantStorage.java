package com.pickeat.backend.participant.domain.storage;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.participant.application.dto.ParticipantStateWithSequenceDto;
import com.pickeat.backend.participant.domain.Participant;
import java.time.Duration;
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

    public Boolean setupAboutParticipant(String pickeatCode, Participant participant) {
        String participantKey = StorageKey.PARTICIPANT.generateKey(pickeatCode);
        String participantCompletionKey = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
        String participantValue = jsonParser.toJson(participant);
        String participantCode = participant.getCode();
        Duration ttl = StorageKey.PICKEAT_TTL;

        return redisTemplate.execute(
                ParticipantStorageScript.ADD_PARTICIPANT_SCRIPT,
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

    public Optional<ParticipantStateWithSequenceDto> getParticipantsState(String pickeatCode) {
        String dataKey = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
        String seqKey = StorageKey.PARTICIPANT_SEQUENCE.generateKey(pickeatCode);

        List<Object> results = redisTemplate.execute(
                ParticipantStorageScript.GET_STATE_SCRIPT,
                List.of(dataKey, seqKey),
                String.valueOf(StorageKey.PICKEAT_TTL.toSeconds())
        );
        return parseParticipantState(results);
    }

    public Optional<ParticipantStateWithSequenceDto> getParticipantsStateWithSequence(String pickeatCode) {
        String dataKey = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
        String seqKey = StorageKey.PARTICIPANT_SEQUENCE.generateKey(pickeatCode);

        List<Object> results = redisTemplate.execute(
                ParticipantStorageScript.GET_STATE_AND_INCR_SEQUENCE_SCRIPT,
                List.of(dataKey, seqKey),
                String.valueOf(StorageKey.PICKEAT_TTL.toSeconds())
        );
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

    private Optional<ParticipantStateWithSequenceDto> parseParticipantState(List<Object> results) {
        if (results == null || results.size() < 2) {
            return Optional.empty();
        }

        List<Object> rawData = (List<Object>) results.get(1);
        if (rawData == null || rawData.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Boolean> dataMap = new HashMap<>();
        for (int i = 0; i < rawData.size(); i += 2) {
            dataMap.put(String.valueOf(rawData.get(i)), Boolean.valueOf(String.valueOf(rawData.get(i + 1))));
        }

        Long sequence = Long.parseLong((String) results.get(0));

        return Optional.of(new ParticipantStateWithSequenceDto(sequence, dataMap));
    }
}
