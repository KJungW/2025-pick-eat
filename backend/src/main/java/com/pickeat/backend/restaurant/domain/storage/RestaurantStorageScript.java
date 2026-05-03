package com.pickeat.backend.restaurant.domain.storage;

import java.util.List;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RestaurantStorageScript {

    public static final DefaultRedisScript<Boolean> SETUP_RESTAURANTS_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    --- 목적: 식당 초기 데이터 세팅 (메타데이터, 생존 식당 코드 목록, 식당별 좋아요수)
                    --- 특징: 멱등성 보장 (이미 데이터가 존재하면 실행하지 않음)
                    
                    --- [Key List]
                    -- KEYS[1]: 전체 식당 메타데이터 키 (JSON)
                    -- KEYS[2]: 생존 식당 코드 목록 키 (Set)
                    -- KEYS[3]: 식당별 좋아요 합계 키 (Hash)
                    
                    --- [ARGV List]
                    -- ARGV[1]: TTL(초)
                    -- ARGV[2]: 전체 식당 메타데이터 JSON
                    -- ARGV[3...]: 전체 식당 코드들
                    
                    -- 1. 이미 저장된 데이터면 실패 처리
                    if redis.call('EXISTS', KEYS[1]) == 1 then
                        return false
                    end
                    
                    -- 2. 식당 메타데이터 저장 및 TTL 설정
                    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[1])
                    
                    -- 3. 생존 식당 코드 목록 저장 / 식당별 좋아요수 저장
                    for i = 3, #ARGV do
                        redis.call('SADD', KEYS[2], ARGV[i])
                        redis.call('HSET', KEYS[3], ARGV[i], 0)
                    end
                    
                    -- 4. 생존 식당 코드 목록 TTL 설정 / 식당별 좋아요수 TTL 설정
                    redis.call('EXPIRE', KEYS[2], ARGV[1])
                    redis.call('EXPIRE', KEYS[3], ARGV[1])
                    
                    return true
                    """, Boolean.class);

    public static final DefaultRedisScript<List> GET_STATE_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    --- 목적: 식당들의 상태 조회 (생존 식당 코드 목록 + 식당별 좋아요수 + 시퀀스)
                    
                    --- [Key List]
                    -- KEYS[1]: 생존 식당 코드 목록 키 (Set)
                    -- KEYS[2]: 식당별 좋아요 합계 키 (Hash)
                    -- KEYS[3]: 시퀀스 키 (Long)
                    
                    -- 1. 생존 식당 코드 목록 + 식당별 좋아요수 조회
                    local alive_codes = redis.call('SMEMBERS', KEYS[1])
                    local like_counts = redis.call('HGETALL', KEYS[2])
                    
                    -- 2. 시퀀스 조회 (없으면 0으로 설정)
                    local sequence = redis.call('GET', KEYS[3])
                    if not sequence then
                        sequence = "0"
                    end
                    
                    return {sequence, alive_codes, like_counts}
                    """, List.class);

    public static final DefaultRedisScript<List> GET_STATE_AND_INCR_SEQUENCE_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    --- 목적: 식당들의 상태 조회 + 시퀀스 번호 증가
                    --- 특징: 시퀀스를 1 증가시킨 후, 변경된 시퀀스와 함께 식당 상태를 원자적으로 반환
                    
                    --- [Key List]
                    -- KEYS[1]: 생존 식당 코드 목록 키 (Set)
                    -- KEYS[2]: 식당별 좋아요 합계 키 (Hash)
                    -- KEYS[3]: 시퀀스 키 (Long)
                    
                    --- [ARGV List]
                    -- ARGV[1]: TTL(초)
                    
                    -- 1. 시퀀스 증가
                    local updated_sequence = redis.call('INCR', KEYS[3])
                    
                    -- 2. 시퀀스가 처음 생성되었을 경우 TTL 세팅
                    if updated_sequence == 1 or redis.call('TTL', KEYS[3]) < 0 then
                        redis.call('EXPIRE', KEYS[3], ARGV[1])
                    end
                    
                    -- 3. 식당들의 상태 조회 (생존 식당 코드 목록 + 식당별 좋아요수)
                    local alive_codes = redis.call('SMEMBERS', KEYS[1])
                    local like_counts = redis.call('HGETALL', KEYS[2])
                    
                    return {updated_sequence, alive_codes, like_counts}
                    """, List.class);

    public static final DefaultRedisScript<Boolean> LIKE_RESTAURANT_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    --- 목적: 식당 좋아요 처리
                    --- 특징: 좋아요를 누르지 않은 상태일 때만 좋아요 처리 수행
                    
                    --- [Key List]
                    -- KEYS[1]: 식당별 참가자의 좋아요 기록 키 (Set)
                    -- KEYS[2]: 식당별 좋아요 합계 키 (Hash)
                    
                    --- [ARGV List]
                    -- ARGV[1]: TTL (초)
                    -- ARGV[2]: 참가자 코드
                    -- ARGV[3]: 식당 코드
                    
                    -- 1. 좋아요 기록에 참가자 추가 시도
                    if redis.call('SADD', KEYS[1], ARGV[2]) == 1 then
                    
                        -- 2. 신규 좋아요라면 해당 식당의 좋아요수를 1 증가
                        redis.call('HINCRBY', KEYS[2], ARGV[3], 1)
                    
                        -- 3. 좋아요 기록 키가 처음 생성되었다면 TTL 설정
                        if redis.call('TTL', KEYS[1]) < 0 then
                            redis.call('EXPIRE', KEYS[1], ARGV[1])
                        end
                        return true
                    end
                    
                    -- 4. 이미 좋아요를 누른 기록이 있다면 false 반환
                    return false
                    """, Boolean.class);

    public static final DefaultRedisScript<Boolean> CANCEL_LIKE_SCRIPT = new DefaultRedisScript<>(
            """
                    --- [Script Overview]
                    --- 목적: 식당 좋아요 취소 처리
                    --- 특징: 좋아요를 누른 상태일 때만 좋아요 취소 처리 실행
                    
                    --- [Key List]
                    -- KEYS[1]: 식당별 참가자의 좋아요 기록 키 (Set)
                    -- KEYS[2]: 식당별 좋아요 합계 키 (Hash)
                    
                    --- [ARGV List]
                    -- ARGV[1]: 참가자 코드
                    -- ARGV[2]: 식당 코드
                    
                    -- 1. 좋아료 기록에서 참가자 제거 시도
                    if redis.call('SREM', KEYS[1], ARGV[1]) == 1 then
                    
                        -- 2. 제거 성공 시, 해당 식당의 좋아요수 1감소
                        redis.call('HINCRBY', KEYS[2], ARGV[2], -1)
                        return true
                    end
                    
                    -- 3. 원래 좋아요를 누르지 않았던 상태라면 false 반환
                    return false
                    """, Boolean.class);

}
