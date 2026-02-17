package com.pickeat.backend.participant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.auth.JwtProvider;
import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequestV2;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponseV2;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponseV2;
import com.pickeat.backend.participant.domain.ParticipantV2;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
        ParticipantServiceV2.class,
        PickeatStorage.class,
        ParticipantStorage.class,
        ParticipantTokenProviderV2.class,
        JwtProvider.class})
class ParticipantServiceV2Test extends DatabaseSliceTest {

    @Autowired
    private ParticipantServiceV2 participantServiceV2;

    @Autowired
    private ParticipantStorage participantStorage;

    @Autowired
    private PickeatStorage pickeatStorage;

    @Autowired
    private ParticipantTokenProviderV2 tokenProvider;

    @Nested
    class 참가자_저장 {

        @Test
        void 참가자를_성공적으로_저장한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("점심 회식");
            pickeatStorage.save(pickeat);
            ParticipantRequestV2 request = new ParticipantRequestV2("참가자1", pickeat.getCode());

            // when
            TokenResponse response = participantServiceV2.createParticipant(request);

            // then
            String token = response.token();
            assertAll(
                    () -> assertThat(token).isNotBlank(),
                    () -> assertThat(tokenProvider.getParticipantCode(token)).isNotBlank(),
                    () -> assertThat(tokenProvider.getPickeatCode(token)).isEqualTo(pickeat.getCode())
            );
        }

        @Test
        void 관련_픽잇이_없는_경우_예외를_발생시킨다() {
            // given
            String invalidCode = "NON_EXISTENT_CODE";
            ParticipantRequestV2 request = new ParticipantRequestV2("참가자1", invalidCode);

            // when & then
            assertThatThrownBy(() -> participantServiceV2.createParticipant(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_메타데이터_조회 {

        @Test
        void 픽잇에_참가한_모든_참가자의_메타데이터를_조회한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("저녁 회식");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            participantServiceV2.createParticipant(new ParticipantRequestV2("참가자1", pickeatCode));
            participantServiceV2.createParticipant(new ParticipantRequestV2("참가자2", pickeatCode));
            participantServiceV2.createParticipant(new ParticipantRequestV2("참가자3", pickeatCode));

            // when
            List<ParticipantResponseV2> result = participantServiceV2.getMetaInPickeat(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(3),
                    () -> assertThat(result)
                            .extracting(ParticipantResponseV2::nickname)
                            .containsExactly("참가자1", "참가자2", "참가자3")
            );
        }

        @Test
        void 픽잇이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String pickeatCode = "EMPTY_PICK_EAT";

            // when & then
            assertThatThrownBy(() -> participantServiceV2.getMetaInPickeat(pickeatCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_상태데이터_조회 {

        @Test
        void 픽잇에_참가한_모든_참가자의_상태데이터를_조회한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("상태 조회 테스트");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            ParticipantV2 participantA = new ParticipantV2("참가자A");
            participantStorage.setupAboutParticipant(pickeatCode, participantA);
            String participantACode = participantA.getCode();

            ParticipantV2 participantB = new ParticipantV2("참가자B");
            participantStorage.setupAboutParticipant(pickeatCode, participantB);
            String participantBCode = participantB.getCode();

            participantServiceV2.markCompletion(pickeatCode, participantACode);

            // when
            ParticipantStateResponseV2 result = participantServiceV2.getStateInPickeat(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result.completion()).hasSize(2),
                    () -> assertThat(result.completion().get(participantACode)).isTrue(),
                    () -> assertThat(result.completion().get(participantBCode)).isFalse()
            );
        }

        @Test
        void 픽잇이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String pickeatCode = "EMPTY_PICK_EAT";

            // when & then
            assertThatThrownBy(() -> participantServiceV2.getMetaInPickeat(pickeatCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_투표_완료_표시 {

        @Test
        void 참가자를_투표_완료_상태로_표시한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("투표 테스트");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            ParticipantV2 participant = new ParticipantV2("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            String participantCode = participant.getCode();

            // when
            participantServiceV2.markCompletion(pickeatCode, participantCode);

            // then
            boolean isCompleted = participantStorage.getParticipantsState(pickeatCode)
                    .completionState()
                    .get(participantCode);
            assertThat(isCompleted).isTrue();
        }

        @Test
        void 픽잇이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String invalidPickeatCode = "INVALID_CODE";
            String participantCode = "any-code";

            // when & then
            assertThatThrownBy(() -> participantServiceV2.markCompletion(invalidPickeatCode, participantCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_투표_완료_표시_취소 {

        @Test
        void 참가자를_투표_완료_상태를_취소한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("취소 테스트");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            ParticipantV2 participant = new ParticipantV2("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            String participantCode = participant.getCode();

            participantServiceV2.markCompletion(pickeatCode, participantCode);

            // when
            participantServiceV2.cancelCompletion(pickeatCode, participantCode);

            // then
            boolean isCompleted = participantStorage.getParticipantsState(pickeatCode)
                    .completionState()
                    .get(participantCode);
            assertThat(isCompleted).isFalse();
        }

        @Test
        void 픽잇이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String invalidPickeatCode = "INVALID_CODE";
            String participantCode = "any-code";

            // when & then
            assertThatThrownBy(() -> participantServiceV2.cancelCompletion(invalidPickeatCode, participantCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }
}
