local cursor = "0"
local total_count = 0

-- 변환 규칙 정의 ([검색 패턴, 데이터 추출을 위한 정규식, 새로운 키 형식] 구성)
local rules = {
    -- 1. Participant Sequence
    {
        old_patt = "pickeat:sequence:participant:*",
        regex = "pickeat:sequence:participant:(%d+)",
        new_fmt = "pickeat:%s:participant:state:sequence"
    },
    -- 2. Restaurant Sequence
    {
        old_patt = "pickeat:sequence:restaurant:*",
        regex = "pickeat:sequence:restaurant:(%d+)",
        new_fmt = "pickeat:%s:restaurant:state:sequence"
    },
    -- 3. Restaurant Meta, Alive, Like Count
    { old_patt = "pickeat:*:restaurants", regex = "pickeat:(%d+):restaurants", new_fmt = "pickeat:%s:restaurant" },
    { old_patt = "pickeat:*:restaurants:alives", regex = "pickeat:(%d+):restaurants:alives", new_fmt = "pickeat:%s:restaurant:alive" },
    { old_patt = "pickeat:*:likes:count", regex = "pickeat:(%d+):likes:count", new_fmt = "pickeat:%s:restaurant:like:count" },
    -- 4. Restaurant Like Record
    {
        old_patt = "pickeat:*:restaurants:*:likes",
        regex = "pickeat:(%d+):restaurants:(%d+):likes",
        new_fmt = "pickeat:%s:restaurant:%s:like:record"
    }
}

-- 규칙마다 마이그레이션을 수행
for _, rule in ipairs(rules) do

    -- 초회 커서 초기화
    cursor = "0"

    repeat
        -- 패턴에 맞는 데이터를 100개씩 조회
        local res = redis.call("SCAN", cursor, "MATCH", rule.old_patt, "COUNT", 100)
        -- 다음 조회를 위한 새로운 커서 위치 저장
        cursor = res[1]
        -- 현재 SCAN에서 찾아낸 키 리스트
        local keys = res[2]

        -- 조회된 키마다 반복
        for _, oldKey in ipairs(keys) do

            -- 정규식을 통해 키 내부 식별값 추출
            local id1, id2 = string.match(oldKey, rule.regex)

            -- 추출된 식별값으로 신버전 키 생성
            if id1 then
                local newKey
                if id2 then
                    newKey = string.format(rule.new_fmt, id1, id2)
                else
                    newKey = string.format(rule.new_fmt, id1)
                end

                -- 신버전 키 저장 (덮어씌우기를 통해 중복 실행 오류 방지)
                redis.call("COPY", oldKey, newKey, "REPLACE")

                -- 구버전 키와 신버전 키의 TLL 동기화
                local ttl = redis.call("TTL", oldKey)
                if ttl > 0 then
                    redis.call("EXPIRE", newKey, ttl)
                end

                -- 성공 횟수 누적
                total_count = total_count + 1
            end
        end
    until cursor == "0"
end

-- 최종적으로 몇개의 키가 마이그레이션 되었는지 반환
return total_count
