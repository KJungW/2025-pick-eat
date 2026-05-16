package com.pickeat.backend.global.argument.resolver;

import com.pickeat.backend.global.argument.annotation.User;
import com.pickeat.backend.global.argument.principal.UserPrincipal;
import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.login.application.UserTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Authorization";
    private final UserTokenProvider userTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(User.class)
                && parameter.getParameterType().equals(UserPrincipal.class);
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
            throw new ClientException(ClientErrorCode.INVALID_AUTH_HEADER);
        }

        return parseUserToken(authHeader);
    }

    private UserPrincipal parseUserToken(String authHeader) {
        String token = authHeader.substring(TOKEN_PREFIX.length());
        Long userId = userTokenProvider.getUserId(token);
        return new UserPrincipal(userId);
    }
}
