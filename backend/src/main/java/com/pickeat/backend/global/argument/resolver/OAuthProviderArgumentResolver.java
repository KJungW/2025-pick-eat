package com.pickeat.backend.global.argument.resolver;

import com.pickeat.backend.global.argument.annotation.OAuthProvider;
import com.pickeat.backend.global.argument.principal.OAuthProviderPrincipal;
import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.login.application.OAuthProviderTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class OAuthProviderArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Authorization";
    private final OAuthProviderTokenProvider providerTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OAuthProvider.class)
                && parameter.getParameterType().equals(OAuthProviderPrincipal.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        String authHeader = webRequest.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_HEADER);
        }

        return parseProviderToken(authHeader);
    }

    private OAuthProviderPrincipal parseProviderToken(String authHeader) {
        String token = authHeader.substring(TOKEN_PREFIX.length());
        Long providerId = providerTokenProvider.getProviderId(token);
        String provider = providerTokenProvider.getProvider(token);
        return new OAuthProviderPrincipal(providerId, provider);
    }
}
