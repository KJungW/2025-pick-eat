package com.pickeat.backend.restaurant.domain.storage;

import java.util.List;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RestaurantStorageScript {

    //TODO: 시퀀스 생성은 세팅 스크립트에서 하도록 변경 (2026-05-17, 일, 20:5)
    public static final DefaultRedisScript<Boolean> SETUP_RESTAURANTS_SCRIPT = new DefaultRedisScript<>(
            """
                    -- [Script Overview]
                    -- 목적: 식당 초기 데이터를 세팅한다
                    -- KEYS: [1] "식당 메타데이터 JSON" 키, [2] "생존 식당 코드 Set" 키, [3] "식당별 좋아요수 Hash" 키 [4] "식당 상태 조회 시퀀스" 키
                    -- ARGS: [1] TTL (초 단위), [2] 전체 식당 JSON 데이터 [3...] 전체 식당 코드들
                    -- 응답: 스크립트 성공 여부
                    
                    -- 이미 저장된 식당 데이터들이라면 종료
                    if redis.call('EXISTS', KEYS[1]) == 1 then
                        return false
                    end
                    
                    -- "식당 메타데이터 JSON" : 전체 식당 JSON 데이터 저장 + TTL 세팅
                    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[1])
                    
                    -- "생존 식당 코드 Set" : 전체 식당을 생종 식당으로 저장 + TTL 세팅
                    for i = 3, #ARGV do
                        redis.call('SADD', KEYS[2], ARGV[i])
                    end
                    redis.call('EXPIRE', KEYS[2], ARGV[1])
                    
                    -- "식당별 좋아요수 Hash" : 전체 식당의 좋아요수를 0으로 저장 + TTL 세팅
                    for i = 3, #ARGV do
                        redis.call('HSET', KEYS[3], ARGV[i], 0)
                    end
                    redis.call('EXPIRE', KEYS[3], ARGV[1])
                    
                    -- "식당 상태 조회 시퀀스" : 시퀀스를 1로 세팅 + TTL 세팅
                    redis.call('SET', KEYS[4], '1', 'EX', ARGV[1])
                    
                    return true
                    """, Boolean.class);

    public static final DefaultRedisScript<List> GET_STATE_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    -- 목적: 식당들의 상태 조회
                    -- KEYS: [1] "생존 식당 코드 Set" 키, [2] "식당별 좋아요수 Hash" 키, [3] "식당 상태 조회 시퀀스" 키
                    -- 응답: { 식당 상태 조회 시퀀스 값 (String),
                              생존 식당 코드 목록 (List),
                              식당별 좋아요수 (Hash:식당코드-좋아요수) }
                    
                    -- "생존 식당 코드 Set" : 투표의 모든 생존 식당 코드 조회
                    local alive_codes = redis.call('SMEMBERS', KEYS[1])
                    
                    -- "식당별 좋아요수 Hash" : 투표의 모든 식당의 좋아요수 조회
                    local like_counts = redis.call('HGETALL', KEYS[2])
                    
                    -- "식당 상태 조회 시퀀스" : 시퀀스 조회
                    local sequence = redis.call('GET', KEYS[3])
                    
                    return {tostring(sequence), alive_codes, like_counts}
                    """, List.class);

    public static final DefaultRedisScript<List> GET_STATE_AND_INCR_SEQUENCE_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    -- 목적: 식당 상태 조회 시퀀스 번호를 1증가 시키고, 식당들의 상태를 조회한다
                    -- KEYS: [1] "생존 식당 코드 Set" 키, [2] "식당별 좋아요수 Hash" 키, [3] "식당 상태 조회 시퀀스" 키
                    -- ARGS: [1] TTL (초 단위)
                    -- 응답: { 식당 상태 조회 시퀀스 값 (String),
                              생존 식당 코드 목록 (List),
                              식당별 좋아요수 (Hash:식당코드-좋아요수) }
                    
                    -- "식당 상태 조회 시퀀스" : 시퀀스 1 증가
                    local sequence = redis.call('INCR', KEYS[3])
                    
                    -- "생존 식당 코드 Set" : 투표의 모든 생존 식당 코드 조회
                    local alive_codes = redis.call('SMEMBERS', KEYS[1])
                    
                    -- "식당별 좋아요수 Hash" : 투표의 모든 식당의 좋아요수 조회
                    local like_counts = redis.call('HGETALL', KEYS[2])
                    
                    return {tostring(sequence), alive_codes, like_counts}
                    """, List.class);

    public static final DefaultRedisScript<Boolean> LIKE_RESTAURANT_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    -- 목적: 식당을 좋아요 처리한다.
                    -- KEYS: [1] "식당별 참가자 좋아요 기록 Set" 키, [2] "식당별 좋아요수 Hash" 키
                    -- ARGS: [1] TTL (초 단위) [2] 참가자 코드 [3] 식당 코드
                    -- 응답: 스크립트 성공 여부
                    
                    -- "식당별 참가자 좋아요 기록 Set" : 참가자 코드 추가
                    if redis.call('SADD', KEYS[1], ARGV[2]) == 1 then
                    
                        -- "식당별 참가자 좋아요 기록 Set" : 처음 생성되었다면 TTL 설정
                        if redis.call('TTL', KEYS[1]) < 0 then
                            redis.call('EXPIRE', KEYS[1], ARGV[1])
                        end
                    
                        -- "식당별 좋아요수 Hash" : 좋아요수 1 증가
                        redis.call('HINCRBY', KEYS[2], ARGV[3], 1)
                        return true
                    end
                    
                    -- 이미 좋아요를 누른 기록이 있다면 false 반환
                    return false
                    """, Boolean.class);

    public static final DefaultRedisScript<Boolean> CANCEL_LIKE_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    -- 목적: 식당 좋아요를 취소한다
                    -- KEYS: [1] "식당별 참가자 좋아요 기록 Set" 키, [2] "식당별 좋아요수 Hash" 키
                    -- ARGS: [1] 참가자 코드 [2] 식당 코드
                    -- 응답: 스크립트 성공 여부
                    
                     -- "식당별 참가자 좋아요 기록 Set" : 참가자 코드 제거
                    if redis.call('SREM', KEYS[1], ARGV[1]) == 1 then
                    
                        -- "식당별 좋아요수 Hash" : 좋아요수 1 감소
                        redis.call('HINCRBY', KEYS[2], ARGV[2], -1)
                        return true
                    end
                    
                    -- 원래 좋아요를 누르지 않았던 상태라면 false 반환
                    return false
                    """, Boolean.class);

}
