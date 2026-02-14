package com.pickeat.backend.participant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.auth.JwtProvider;
import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequestV2;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
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
                    .hasMessageContaining(ErrorCode.PICKEAT_NOT_FOUND.getMessage());
        }
    }
}
