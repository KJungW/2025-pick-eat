package com.pickeat.backend.participant.domain.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.participant.application.dto.ParticipantStateWithSequenceDto;
import com.pickeat.backend.participant.domain.Participant;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.List;
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

    @Nested
    class 참가자_저장 {

        @Test
        void 참가자를_성공적으로_저장한다() {
            // given
            String pickeatCode = "pickeat-code";
            Participant participant = new Participant("닉네임");

            // when
            participantStorage.setupAboutParticipant(pickeatCode, participant);

            // then
            List<Participant> allParticipants = participantStorage.getParticipantsMeta(pickeatCode);
            ParticipantStateWithSequenceDto participantsState = participantStorage.getParticipantsState(pickeatCode)
                    .get();
            assertAll(
                    () -> assertThat(allParticipants).hasSize(1),
                    () -> assertThat(allParticipants)
                            .extracting(Participant::getCode)
                            .containsExactly(participant.getCode()),
                    () -> assertThat(participantsState.completionState().get(participant.getCode())).isFalse()
            );
        }

        @Test
        void 픽잇의_첫_참가자를_생성할_때만_해시데이터의_TTL을_설정한다() throws InterruptedException {
            // given
            String pickeatCode = "ttl-test-code";
            String key = StorageKey.PARTICIPANT.generateKey(pickeatCode);
            long configTtl = StorageKey.PICKEAT_TTL.getSeconds();

            Participant firstParticipant = new Participant("첫번째");
            Participant secondParticipant = new Participant("두번째");

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
            Participant participant1 = new Participant("참가자1");
            participantStorage.setupAboutParticipant(pickeatCode, participant1);
            Participant participant2 = new Participant("참가자2");
            participantStorage.setupAboutParticipant(pickeatCode, participant2);

            // when
            List<Participant> result = participantStorage.getParticipantsMeta(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result)
                            .extracting(Participant::getCode)
                            .containsExactlyInAnyOrder(participant1.getCode(), participant2.getCode())
            );
        }

        @Test
        void 존재하지_않는_픽잇코드의_메타데이터_조회_시_빈_리스트를_반환한다() {
            // given
            String invalidPickeatCode = "not-exist-code";

            // when
            List<Participant> result = participantStorage.getParticipantsMeta(invalidPickeatCode);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 참가자_상태_조회 {

        @Test
        void 참가자_상태를_성공적으로_조회한다() {
            // given
            String pickeatCode = "pickeat-code";

            Participant participant = new Participant("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            participantStorage.markCompletion(pickeatCode, participant.getCode());

            // when
            Optional<ParticipantStateWithSequenceDto> result = participantStorage.getParticipantsState(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().sequence()).isEqualTo(0),
                    () -> assertThat(result.get().completionState().get(participant.getCode())).isTrue()
            );
        }

        @Test
        void 존재하지_않는_픽잇코드의_상태_조회_시_Empty_Optional을_반환한다() {
            // given
            String invalidPickeatCode = "not-exist-code";

            // when
            Optional<ParticipantStateWithSequenceDto> result =
                    participantStorage.getParticipantsState(invalidPickeatCode);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 참가자_상태_조회하고_시퀀스_번호_증가 {

        @Test
        void 참가자_상태를_성공적으로_조회한다() {
            // given
            String pickeatCode = "pickeat-code";
            Participant participant = new Participant("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            participantStorage.markCompletion(pickeatCode, participant.getCode());

            // when
            Optional<ParticipantStateWithSequenceDto> result =
                    participantStorage.getParticipantsStateWithSequence(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().sequence()).isEqualTo(1L),
                    () -> assertThat(result.get().completionState().get(participant.getCode())).isTrue()
            );
        }

        @Test
        void 참가자_상태_시퀀스_번호를_성공적으로_증가시킨다() {
            // given
            String pickeatCode = "pickeat-code";
            participantStorage.setupAboutParticipant(pickeatCode, new Participant("참가자"));

            // when
            participantStorage.getParticipantsStateWithSequence(pickeatCode); // seq: 1
            participantStorage.getParticipantsStateWithSequence(pickeatCode); // seq: 2
            Optional<ParticipantStateWithSequenceDto> result =
                    participantStorage.getParticipantsStateWithSequence(pickeatCode); // seq: 3

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().sequence()).isEqualTo(3L)
            );
        }

        @Test
        void 시퀀스_번호를_처음_생성할때_TTL을_설정한다() {
            // given
            String pickeatCode = "seq-ttl-test";
            String seqKey = StorageKey.PARTICIPANT_SEQUENCE.generateKey(pickeatCode);
            Long configTtl = StorageKey.PICKEAT_TTL.toSeconds();

            // when: 시퀀스가 처음 생성됨 (1로 증가)
            participantStorage.getParticipantsStateWithSequence(pickeatCode);
            Long ttl = redisTemplate.getExpire(seqKey);

            // then
            assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(configTtl);
        }

        @Test
        void 존재하지_않는_픽잇코드의_상태_조회_시_Empty_Optional을_반환한다() {
            // given
            String invalidPickeatCode = "not-exist-code";

            // when
            Optional<ParticipantStateWithSequenceDto> result =
                    participantStorage.getParticipantsStateWithSequence(invalidPickeatCode);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 참가자_투표_완료_표시 {

        @Test
        void 참가자를_투표_완료_상태로_변경한다() {
            // given
            String pickeatCode = "pickeat-code";
            Participant participant = new Participant("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);

            // when
            participantStorage.markCompletion(pickeatCode, participant.getCode());

            // then
            ParticipantStateWithSequenceDto result = participantStorage.getParticipantsState(pickeatCode).get();
            assertThat(result.completionState().get(participant.getCode())).isTrue();
        }
    }

    @Nested
    class 참가자_투표_완료_표시_제거 {

        @Test
        void 참가자의_투표_완료_상태를_취소한다() {
            // given
            String pickeatCode = "pickeat-code";
            Participant participant = new Participant("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            participantStorage.markCompletion(pickeatCode, participant.getCode());

            // when
            participantStorage.cancelCompletion(pickeatCode, participant.getCode());

            // then
            ParticipantStateWithSequenceDto result = participantStorage.getParticipantsState(pickeatCode).get();
            assertThat(result.completionState().get(participant.getCode())).isFalse();
        }
    }

    @Nested
    class 픽잇_참가자_관련_데이터_제거 {

        @Test
        void 픽잇과_관련된_모든_참가자_데이터를_제거할_수_있다() {
            // given
            String pickeatCode = "remove-participant-code";
            Participant participant = new Participant("참가자");

            participantStorage.setupAboutParticipant(pickeatCode, participant);
            participantStorage.markCompletion(pickeatCode, participant.getCode());
            participantStorage.getParticipantsStateWithSequence(pickeatCode);

            String participantKey = StorageKey.PARTICIPANT.generateKey(pickeatCode);
            String completionKey = StorageKey.PARTICIPANT_COMPLETION.generateKey(pickeatCode);
            String sequenceKey = StorageKey.PARTICIPANT_SEQUENCE.generateKey(pickeatCode);

            // 각 키가 존재하는지 사전 검증
            assertAll(
                    () -> assertThat(redisTemplate.hasKey(participantKey)).isTrue(),
                    () -> assertThat(redisTemplate.hasKey(completionKey)).isTrue(),
                    () -> assertThat(redisTemplate.hasKey(sequenceKey)).isTrue()
            );

            // when
            participantStorage.remove(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(redisTemplate.hasKey(participantKey)).isFalse(),
                    () -> assertThat(redisTemplate.hasKey(completionKey)).isFalse(),
                    () -> assertThat(redisTemplate.hasKey(sequenceKey)).isFalse(),
                    () -> assertThat(participantStorage.getParticipantsMeta(pickeatCode)).isEmpty(),
                    () -> assertThat(participantStorage.getParticipantsState(pickeatCode)).isEmpty()
            );
        }
    }
}
