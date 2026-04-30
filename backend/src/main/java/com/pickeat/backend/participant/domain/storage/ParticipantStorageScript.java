package com.pickeat.backend.participant.domain.storage;

import java.util.List;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class ParticipantStorageScript {

    public static final DefaultRedisScript<Boolean> ADD_PARTICIPANT_SCRIPT = new DefaultRedisScript<>(
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

    public static final DefaultRedisScript<List> GET_STATE_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 참가자 완료 여부 해시 키 (HASH)
                    -- KEYS[2]: 시퀀스 키 (STRING)
                    
                    -- 1. 현재 해시 데이터 전체 조회
                    local data = redis.call('HGETALL', KEYS[1])
                    
                    -- 2. 시퀀스 값 단순 조회
                    local seq = redis.call('GET', KEYS[2])
                    
                    -- 3. 값이 없을 경우 처리
                    if not seq then
                        seq = "0"
                    end
                    
                    return {tostring(seq), data}
                    """, List.class);

    public static final DefaultRedisScript<List> GET_STATE_AND_INCR_SEQUENCE_SCRIPT = new DefaultRedisScript<>(
            """
                    -- KEYS[1]: 참가자 완료 여부 해시 키 (HASH)
                    -- KEYS[2]: 시퀀스 키 (STRING)
                    -- ARGV[1]: TTL (초 단위)
                    
                    -- 1. 현재 해시 데이터 전체 조회
                    local data = redis.call('HGETALL', KEYS[1])
                    
                    -- 2. 시퀀스 무조건 증가
                    local seq = redis.call('INCR', KEYS[2])
                    
                    -- 3. 처음 시퀀스 생성 시 TTL 설정
                    if tonumber(seq) == 1 then
                        redis.call('EXPIRE', KEYS[2], ARGV[1])
                    end
                    
                    return {tostring(seq), data}
                    """, List.class);
}
