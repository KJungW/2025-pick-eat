package com.pickeat.backend.global.auth;

import com.pickeat.backend.global.auth.annotation.ParticipantInPickeatV2;
import com.pickeat.backend.global.auth.principal.ParticipantPrincipalV2;
import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.participant.application.ParticipantTokenProviderV2;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class ParticipantInPickeatArgumentResolverV2 implements HandlerMethodArgumentResolver {

    private static final String PREFIX = "Bearer ";
    private final ParticipantTokenProviderV2 participantTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ParticipantInPickeatV2.class)
                && parameter.getParameterType().equals(ParticipantPrincipalV2.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        ParticipantInPickeatV2 participantInPickeatAnnotation = parameter.getParameterAnnotation(
                ParticipantInPickeatV2.class);
        boolean required = participantInPickeatAnnotation.required();

        String authHeader = webRequest.getHeader("Pickeat-Participant-Token");

        if (!hasAuthToken(authHeader)) {
            if (required) {
                throw new BusinessException(ErrorCode.HEADER_IS_EMPTY);
            }
            return null;
        }

        return getParticipantPrincipalByHeader(authHeader);
    }

    private boolean hasAuthToken(String authHeader) {
        return authHeader != null && authHeader.startsWith(PREFIX);
    }

    private ParticipantPrincipalV2 getParticipantPrincipalByHeader(String authHeader) {
        String token = authHeader.substring(PREFIX.length());
        String participantCode = participantTokenProvider.getParticipantCode(token);
        String pickeatCode = participantTokenProvider.getPickeatCode(token);
        return new ParticipantPrincipalV2(participantCode, pickeatCode);
    }
}
