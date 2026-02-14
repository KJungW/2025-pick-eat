package com.pickeat.backend.participant.domain.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import com.pickeat.backend.participant.domain.ParticipantV2;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.List;
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

    @Nested
    class 참가자_저장 {

        @Test
        void 참가자를_성공적으로_저장한다() {
            // given
            String pickeatCode = "pickeat-code";
            ParticipantV2 participant = new ParticipantV2("닉네임");

            // when
            participantStorage.setupAboutParticipant(pickeatCode, participant);

            // then
            List<ParticipantV2> allParticipants = participantStorage.getParticipantsMeta(pickeatCode);
            ParticipantStateDto participantsState = participantStorage.getParticipantsState(pickeatCode);
            assertAll(
                    () -> assertThat(allParticipants).hasSize(1),
                    () -> assertThat(allParticipants)
                            .extracting(ParticipantV2::getCode)
                            .containsExactly(participant.getCode()),
                    () -> assertThat(participantsState.completionState().get(participant.getCode())).isFalse()
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
            participantStorage.setupAboutParticipant(pickeatCode, firstParticipant);
            long firstTtl = redisTemplate.getExpire(key);

            // then: 설정된 TTL이 예상 범위 내에 있는지 확인
            assertThat(firstTtl).isBetween(1L, configTtl);

            Thread.sleep(1100);

            // when: 두 번째 참가자 저장 (이미 TTL이 존재하므로 Lua 스크립트에 의해 EXPIRE가 실행되지 않아야 함)
            participantStorage.setupAboutParticipant(pickeatCode, secondParticipant);
            long secondTtl = redisTemplate.getExpire(key);

            // then: 두 번째 저장 후에도 TTL이 재설정(Reset)되지 않고 첫 번째 확인 시점보다 작거나 같아야 함
            assertThat(secondTtl).isLessThan(firstTtl);
        }
    }

    @Nested
    class 참가자_메타데이터_조회 {

        @Test
        void 참가자_메타데이터를_성공적으로_조회한다() {
            // given
            String pickeatCode = "pickeat-code";
            ParticipantV2 participant1 = new ParticipantV2("참가자1");
            participantStorage.setupAboutParticipant(pickeatCode, participant1);
            ParticipantV2 participant2 = new ParticipantV2("참가자2");
            participantStorage.setupAboutParticipant(pickeatCode, participant2);

            // when
            List<ParticipantV2> result = participantStorage.getParticipantsMeta(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result)
                            .extracting(ParticipantV2::getCode)
                            .containsExactlyInAnyOrder(participant1.getCode(), participant2.getCode())
            );
        }
    }

    @Nested
    class 참가자_상태_조회 {

        @Test
        void 참가자_상태를_성공적으로_조회한다() {
            // given
            String pickeatCode = "pickeat-code";

            ParticipantV2 participant = new ParticipantV2("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            participantStorage.markCompletion(pickeatCode, participant.getCode());

            // when
            ParticipantStateDto result = participantStorage.getParticipantsState(pickeatCode);

            // then
            assertThat(result.completionState().get(participant.getCode())).isTrue();
        }
    }

    @Nested
    class 참가자_투표_완료_표시 {

        @Test
        void 참가자를_투표_완료_상태로_변경한다() {
            // given
            String pickeatCode = "pickeat-code";
            ParticipantV2 participant = new ParticipantV2("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);

            // when
            participantStorage.markCompletion(pickeatCode, participant.getCode());

            // then
            ParticipantStateDto result = participantStorage.getParticipantsState(pickeatCode);
            assertThat(result.completionState().get(participant.getCode())).isTrue();
        }
    }

    @Nested
    class 참가자_투표_완료_표시_제거 {

        @Test
        void 참가자의_투표_완료_상태를_취소한다() {
            // given
            String pickeatCode = "pickeat-code";
            ParticipantV2 participant = new ParticipantV2("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            participantStorage.markCompletion(pickeatCode, participant.getCode());

            // when
            participantStorage.cancelCompletion(pickeatCode, participant.getCode());

            // then
            ParticipantStateDto result = participantStorage.getParticipantsState(pickeatCode);
            assertThat(result.completionState().get(participant.getCode())).isFalse();
        }
    }

    @Nested
    class 픽잇_참가자_관련_데이터_제거 {

        @Test
        void 픽잇과_관련된_모든_참가자_데이터를_제거할_수_있다() {
            // given
            String pickeatCode = "remove-test-code";
            ParticipantV2 participant1 = new ParticipantV2("참가자1");
            ParticipantV2 participant2 = new ParticipantV2("참가자2");

            participantStorage.setupAboutParticipant(pickeatCode, participant1);
            participantStorage.setupAboutParticipant(pickeatCode, participant2);

            // when
            participantStorage.remove(pickeatCode);

            // then
            List<ParticipantV2> remainingParticipants = participantStorage.getParticipantsMeta(pickeatCode);
            assertThat(remainingParticipants).isEmpty();
        }
    }
}
