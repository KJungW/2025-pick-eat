package com.pickeat.backend.pickeat.domain.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.pickeat.domain.ParticipantV2;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(ParticipantStorage.class)
class ParticipantStorageTest extends DatabaseSliceTest {

    @Autowired
    private ParticipantStorage participantStorage;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JsonParser jsonParser;

    @Nested
    class 참가자_저장 {

        @Test
        void 참가자를_성공적으로_저장한다() {
            // given
            String pickeatCode = "pickeat-code";
            ParticipantV2 participant = new ParticipantV2("닉네임");
            String expectedKey = StorageKey.PARTICIPANT.generateKey(pickeatCode);

            // when
            participantStorage.save(pickeatCode, participant);

            // then
            Object savedValue = redisTemplate.opsForHash().get(expectedKey, participant.getCode());
            ParticipantV2 saved = jsonParser.fromJson((String) savedValue, ParticipantV2.class);

            assertAll(
                    () -> assertThat(saved).isNotNull(),
                    () -> assertThat(saved.getCode()).isEqualTo(participant.getCode())
            );
        }

        @Test
        void 픽잇의_첫_참가자를_생성할_때만_해시데이터의_TTL을_설정한다() throws InterruptedException {
            // given
            String pickeatCode = "ttl-test-code";
            String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
            long configTtl = StorageKey.PARTICIPANT.getTtl().getSeconds();

            ParticipantV2 firstParticipant = new ParticipantV2("첫번째");
            ParticipantV2 secondParticipant = new ParticipantV2("두번째");

            // when: 첫 번째 참가자 저장 (이때 TTL이 설정됨)
            participantStorage.save(pickeatCode, firstParticipant);
            long firstTtl = redisTemplate.getExpire(key);

            // then: 설정된 TTL이 예상 범위 내에 있는지 확인
            assertThat(firstTtl).isBetween(1L, configTtl);

            Thread.sleep(1100);

            // when: 두 번째 참가자 저장 (이미 TTL이 존재하므로 Lua 스크립트에 의해 EXPIRE가 실행되지 않아야 함)
            participantStorage.save(pickeatCode, secondParticipant);
            long secondTtl = redisTemplate.getExpire(key);

            // then: 두 번째 저장 후에도 TTL이 재설정(Reset)되지 않고 첫 번째 확인 시점보다 작거나 같아야 함
            assertThat(secondTtl).isLessThan(firstTtl);
        }
    }

    @Nested
    class 참가자_조회 {

        @Test
        void 참가자를_성공적으로_조회한다() {
            // given
            String pickeatCode = "pickeat-code";
            ParticipantV2 participant = new ParticipantV2("조회대상");
            participantStorage.save(pickeatCode, participant);

            // when
            Optional<ParticipantV2> result = participantStorage.get(pickeatCode, participant.getCode());

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getCode()).isEqualTo(participant.getCode())
            );
        }
    }
}
