package com.pickeat.backend.global.argument.resolver;

import com.pickeat.backend.global.argument.annotation.Participant;
import com.pickeat.backend.global.argument.principal.ParticipantPrincipal;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.participant.application.ParticipantTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class ParticipantArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Pickeat-Participant-Token";
    private final ParticipantTokenProvider participantTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Participant.class)
                && parameter.getParameterType().equals(ParticipantPrincipal.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        String authHeader = webRequest.getHeader(AUTH_HEADER);

        if (isEmptyToken(authHeader)) {
            if (isRequiredToken(parameter)) {
                throw new BusinessException(ErrorCode.INVALID_AUTH_HEADER);
            }
            return null;
        }

        return parseParticipantToken(authHeader);
    }

    private boolean isRequiredToken(MethodParameter parameter) {
        Participant participantAnnotation = parameter.getParameterAnnotation(Participant.class);
        return participantAnnotation.required();
    }

    private boolean isEmptyToken(String authHeader) {
        return authHeader == null || !authHeader.startsWith(TOKEN_PREFIX);
    }

    private ParticipantPrincipal parseParticipantToken(String authHeader) {
        String token = authHeader.substring(TOKEN_PREFIX.length());
        String participantCode = participantTokenProvider.getParticipantCode(token);
        String pickeatCode = participantTokenProvider.getPickeatCode(token);
        return new ParticipantPrincipal(participantCode, pickeatCode);
    }
}
