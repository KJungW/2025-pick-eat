package com.pickeat.backend.global.argument.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import com.pickeat.backend.global.argument.annotation.Participant;
import com.pickeat.backend.global.argument.principal.ParticipantPrincipal;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.participant.application.ParticipantTokenProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class ParticipantArgumentResolverTest {

    @InjectMocks
    private ParticipantArgumentResolver participantArgumentResolver;

    @Mock
    private ParticipantTokenProvider participantTokenProvider;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private Participant participantAnnotation;

    private static final String VALID_TOKEN = "valid.participant.token";
    private static final String AUTH_HEADER = "Bearer " + VALID_TOKEN;
    private static final String HEADER_NAME = "Pickeat-Participant-Token";

    @Nested
    class 파라미터_지원_여부_확인 {

        @Test
        void 파라미터에_적용된_애노테이션과_타입을_확인한다() {
            // given
            given(methodParameter.hasParameterAnnotation(Participant.class)).willReturn(true);
            doReturn(ParticipantPrincipal.class).when(methodParameter).getParameterType();

            // when
            boolean result = participantArgumentResolver.supportsParameter(methodParameter);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class 인자_Resolving {

        @Test
        void 인자를_적절히_리졸빙할_수_있다() {
            // given
            given(webRequest.getHeader(HEADER_NAME)).willReturn(AUTH_HEADER);
            given(participantTokenProvider.getParticipantCode(VALID_TOKEN)).willReturn("PARTICIPANT_1");
            given(participantTokenProvider.getPickeatCode(VALID_TOKEN)).willReturn("PICKEAT_1");

            // when
            Object result = participantArgumentResolver.resolveArgument(methodParameter, null, webRequest, null);

            // then
            ParticipantPrincipal principal = (ParticipantPrincipal) result;
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result).isInstanceOf(ParticipantPrincipal.class),
                    () -> assertThat(principal.pickeatCode()).isEqualTo("PICKEAT_1"),
                    () -> assertThat(principal.participantCode()).isEqualTo("PARTICIPANT_1")
            );
        }

        @Test
        void 필수값이_아니라면_토큰이_존재하지_않아도_된다() {
            // given
            given(webRequest.getHeader(HEADER_NAME)).willReturn(null);
            given(methodParameter.getParameterAnnotation(Participant.class)).willReturn(participantAnnotation);
            given(participantAnnotation.required()).willReturn(false);

            // when
            Object result = participantArgumentResolver.resolveArgument(methodParameter, null, webRequest, null);

            // then
            assertThat(result).isNull();
        }

        @Test
        void 헤더가_비어있는_경우_예외처리() {
            // given
            given(webRequest.getHeader(HEADER_NAME)).willReturn(null);
            given(methodParameter.getParameterAnnotation(Participant.class)).willReturn(participantAnnotation);
            given(participantAnnotation.required()).willReturn(true);

            // when & then
            assertThatThrownBy(
                    () -> participantArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(ClientException.class)
                    .hasMessage(ErrorCode.INVALID_AUTH_HEADER.getMessage());
        }

        @Test
        void 헤더_prefix가_올바르지_않은_경우_예외처리() {
            // given
            given(webRequest.getHeader(HEADER_NAME)).willReturn("Invalid " + VALID_TOKEN);
            given(methodParameter.getParameterAnnotation(Participant.class)).willReturn(participantAnnotation);
            given(participantAnnotation.required()).willReturn(true);

            // when & then
            assertThatThrownBy(
                    () -> participantArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(ClientException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_AUTH_HEADER);
        }

        @Test
        void 토큰이_올바르지_않은_경우_예외처리() {
            // given
            given(webRequest.getHeader(HEADER_NAME)).willReturn(AUTH_HEADER);
            given(participantTokenProvider.getParticipantCode(VALID_TOKEN))
                    .willThrow(new ClientException(ErrorCode.INVALID_TOKEN));

            // when & then
            assertThatThrownBy(
                    () -> participantArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(ClientException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }
}
