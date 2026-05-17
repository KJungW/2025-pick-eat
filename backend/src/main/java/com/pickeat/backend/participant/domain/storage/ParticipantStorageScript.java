package com.pickeat.backend.participant.domain.storage;

import java.util.List;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class ParticipantStorageScript {

    public static final DefaultRedisScript<Boolean> ADD_PARTICIPANT_SCRIPT = new DefaultRedisScript<>(
            """
                    -- [Script Overview]
                    -- 목적: 참가자를 추가한다 ("참가자 List"와 "참가자 완료 여부 Hash"에 참가자 추가)
                    -- KEYS: [1] "참가자 List" 키, [2] "참가자 완료 여부 Hash" 키
                    -- ARGS: [1] TTL (초 단위), [2] 참가자 JSON 데이터, [3] 참가자 코드
                    -- 응답: 스크립트 성공 여부
                    
                    -- "참가자 List" : 새로운 참가자 추가
                    redis.call('RPUSH', KEYS[1], ARGV[2])
                    
                    -- "참가자 완료 여부 Hash" : 새로운 참가자 추가
                    redis.call('HSET', KEYS[2], ARGV[3], 'false')
                    
                    -- "참가자 List" : 만약 처음 생성되었다면 TTL 설정
                    if redis.call('TTL', KEYS[1]) < 0 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end
                    
                    -- "참가자 완료 여부 Hash" : 처음 생성되었다면 TTL 설정
                    if redis.call('TTL', KEYS[2]) < 0 then
                        redis.call('EXPIRE', KEYS[2], ARGV[1])
                    end
                    
                    return true
                    """, Boolean.class);

    public static final DefaultRedisScript<List> GET_ALL_STATE_SCRIPT = new DefaultRedisScript<>(
            """
                    -- [Script Overview]
                    -- 목적: 현재 투표의 모든 참가자 상태를 조회한다. ("참가자 완료 여부 Hash"와 "참가자 상태 시퀀스" 조회)
                    -- KEYS: [1] "참가자 완료 여부 Hash" 키, [2] "참가자 상태 시퀀스" 키
                    -- 응답: { 참가자 상태 시퀀스 값 (String), 모든 참가자의 투표 완료 여부 (Hash:참가자코드-완료여부) }
                    
                    -- "참가자 완료 여부 Hash" : 투표에 해당하는 모든 참가자 완료 여부 조회
                    local data = redis.call('HGETALL', KEYS[1])
                    
                    -- "참가자 상태 시퀀스" : 참가자 상태 시퀀스 조회
                    local seq = redis.call('GET', KEYS[2])
                    
                    -- "참가자 상태 시퀀스" : 참가자 상태 시퀀스 값이 없을 경우 0으로 세팅
                    if not seq then
                        seq = "0"
                    end
                    
                    return {tostring(seq), data}
                    """, List.class);

    public static final DefaultRedisScript<List> GET_ALL_STATE_AND_INCR_SEQUENCE_SCRIPT = new DefaultRedisScript<>(
            """
                    -- [Script Overview]
                    -- 목적: 시퀀스 번호를 1증가 시키고, 현재 투표의 모든 참가자 상태를 조회한다.
                            ("참가자 상태 시퀀스" 증가 후에 "참가자 완료 여부 Hash"와 "참가자 상태 시퀀스" 조회)
                    -- KEYS: [1] "참가자 완료 여부 Hash" 키, [2] "참가자 상태 시퀀스" 키
                    -- ARGS: [1] TTL (초 단위)
                    -- 응답: { 참가자 상태 시퀀스 값 (String), 모든 참가자의 투표 완료 여부 (Hash:참가자코드-완료여부) }
                    
                    -- "참가자 상태 시퀀스" : 1 증가시킨 후에 조회
                    local seq = redis.call('INCR', KEYS[2])
                    
                    -- "참가자 상태 시퀀스" : 처음 시퀀스 생성 시 TTL 설정
                    if tonumber(seq) == 1 then
                        redis.call('EXPIRE', KEYS[2], ARGV[1])
                    end
                    
                    -- "참가자 완료 여부 Hash" : 투표에 해당하는 모든 참가자 완료 여부 조회
                    local data = redis.call('HGETALL', KEYS[1])
                    
                    return {tostring(seq), data}
                    """, List.class);
}
