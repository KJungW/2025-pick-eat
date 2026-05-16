package com.pickeat.backend.participant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.global.utility.JwtProvider;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEventRequest;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequest;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponse;
import com.pickeat.backend.participant.domain.Participant;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;

@Import({
        ParticipantService.class,
        PickeatStorage.class,
        ParticipantStorage.class,
        ParticipantTokenProvider.class,
        JwtProvider.class})
class ParticipantServiceTest extends DatabaseSliceTest {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ParticipantStorage participantStorage;

    @Autowired
    private PickeatStorage pickeatStorage;

    @Autowired
    private ParticipantTokenProvider tokenProvider;

    @Autowired
    private ApplicationEvents events;

    @Nested
    class 참가자_저장 {

        @Test
        void 참가자를_성공적으로_저장한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("점심 회식");
            pickeatStorage.save(pickeat);
            ParticipantRequest request = new ParticipantRequest("참가자1", pickeat.getCode());

            // when
            TokenResponse response = participantService.createParticipant(request);

            // then
            String token = response.token();
            assertAll(
                    () -> assertThat(token).isNotBlank(),
                    () -> assertThat(tokenProvider.getParticipantCode(token)).isNotBlank(),
                    () -> assertThat(tokenProvider.getPickeatCode(token)).isEqualTo(pickeat.getCode())
            );
        }

        @Test
        void 참가자_업데이트_이벤트를_발행한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("이벤트 발행 테스트");
            pickeatStorage.save(pickeat);
            ParticipantRequest request = new ParticipantRequest("참가자1", pickeat.getCode());

            // when
            participantService.createParticipant(request);

            // then
            Long count = events.stream(ParticipantUpdateEventRequest.class)
                    .filter(event -> event.pickeatCode().equals(pickeat.getCode()))
                    .count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        void 관련_픽잇이_없는_경우_예외를_발생시킨다() {
            // given
            String invalidCode = "NON_EXISTENT_CODE";
            ParticipantRequest request = new ParticipantRequest("참가자1", invalidCode);

            // when & then
            assertThatThrownBy(() -> participantService.createParticipant(request))
                    .isInstanceOf(ClientException.class)
                    .hasMessageContaining(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_메타데이터_조회 {

        @Test
        void 픽잇에_참가한_모든_참가자의_메타데이터를_조회한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("저녁 회식");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            participantService.createParticipant(new ParticipantRequest("참가자1", pickeatCode));
            participantService.createParticipant(new ParticipantRequest("참가자2", pickeatCode));
            participantService.createParticipant(new ParticipantRequest("참가자3", pickeatCode));

            // when
            List<ParticipantResponse> result = participantService.getMetaInPickeat(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(3),
                    () -> assertThat(result)
                            .extracting(ParticipantResponse::nickname)
                            .containsExactly("참가자1", "참가자2", "참가자3")
            );
        }

        @Test
        void 픽잇이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String pickeatCode = "EMPTY_PICK_EAT";

            // when & then
            assertThatThrownBy(() -> participantService.getMetaInPickeat(pickeatCode))
                    .isInstanceOf(ClientException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_상태데이터_조회 {

        @Test
        void 픽잇에_참가한_모든_참가자의_상태데이터를_조회한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("상태 조회 테스트");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            Participant participantA = new Participant("참가자A");
            participantStorage.setupAboutParticipant(pickeatCode, participantA);
            String participantACode = participantA.getCode();

            Participant participantB = new Participant("참가자B");
            participantStorage.setupAboutParticipant(pickeatCode, participantB);
            String participantBCode = participantB.getCode();

            participantService.markCompletion(pickeatCode, participantACode);

            // when
            ParticipantStateResponse result = participantService.getStateInPickeat(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result.sequence()).isEqualTo(0),
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
            assertThatThrownBy(() -> participantService.getMetaInPickeat(pickeatCode))
                    .isInstanceOf(ClientException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_투표_완료_표시 {

        @Test
        void 참가자를_투표_완료_상태로_표시한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("투표 테스트");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            Participant participant = new Participant("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            String participantCode = participant.getCode();

            // when
            participantService.markCompletion(pickeatCode, participantCode);

            // then
            Boolean isCompleted = participantStorage.getParticipantsState(pickeatCode).get()
                    .completionState()
                    .get(participantCode);
            assertThat(isCompleted).isTrue();
        }

        @Test
        void 참가자_업데이트_이벤트를_발행한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("이벤트 발행 테스트");
            pickeatStorage.save(pickeat);
            ParticipantRequest request = new ParticipantRequest("참가자1", pickeat.getCode());

            // when
            participantService.createParticipant(request);

            // then
            Long count = events.stream(ParticipantUpdateEventRequest.class)
                    .filter(event -> event.pickeatCode().equals(pickeat.getCode()))
                    .count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        void 픽잇이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String invalidPickeatCode = "INVALID_CODE";
            String participantCode = "any-code";

            // when & then
            assertThatThrownBy(() -> participantService.markCompletion(invalidPickeatCode, participantCode))
                    .isInstanceOf(ClientException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 참가자_투표_완료_표시_취소 {

        @Test
        void 참가자를_투표_완료_상태를_취소한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("취소 테스트");
            pickeatStorage.save(pickeat);
            String pickeatCode = pickeat.getCode();

            Participant participant = new Participant("참가자");
            participantStorage.setupAboutParticipant(pickeatCode, participant);
            String participantCode = participant.getCode();

            participantService.markCompletion(pickeatCode, participantCode);

            // when
            participantService.cancelCompletion(pickeatCode, participantCode);

            // then
            Boolean isCompleted = participantStorage.getParticipantsState(pickeatCode).get()
                    .completionState()
                    .get(participantCode);
            assertThat(isCompleted).isFalse();
        }

        @Test
        void 참가자_업데이트_이벤트를_발행한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("취소 테스트");
            pickeatStorage.save(pickeat);
            Participant participant = new Participant("참가자");
            participantStorage.setupAboutParticipant(pickeat.getCode(), participant);
            participantService.markCompletion(pickeat.getCode(), participant.getCode());

            // when
            participantService.cancelCompletion(pickeat.getCode(), participant.getCode());

            // then
            Long count = events.stream(ParticipantUpdateEventRequest.class)
                    .filter(event -> event.pickeatCode().equals(pickeat.getCode()))
                    .count();
            assertThat(count).isEqualTo(2);
        }

        @Test
        void 픽잇이_존재하지_않는다면_예외를_발생시킨다() {
            // given
            String invalidPickeatCode = "INVALID_CODE";
            String participantCode = "any-code";

            // when & then
            assertThatThrownBy(() -> participantService.cancelCompletion(invalidPickeatCode, participantCode))
                    .isInstanceOf(ClientException.class)
                    .hasMessage(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }
}
