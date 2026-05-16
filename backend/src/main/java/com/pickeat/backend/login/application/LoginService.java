package com.pickeat.backend.login.application;

import com.pickeat.backend.global.argument.principal.OAuthProviderPrincipal;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.login.application.dto.request.AuthCodeRequest;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.user.domain.User;
import com.pickeat.backend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginClient loginClient;
    private final JwtOidProvider jwtOIDProvider;
    private final UserRepository userRepository;
    private final UserTokenProvider userTokenProvider;

    public Long getProviderIdFromIdToken(AuthCodeRequest request) {
        String idTokenJwt = loginClient.getIdToken(request.code(), request.redirectUrl());
        Long providerId = jwtOIDProvider.extractProviderIdFromIdToken(idTokenJwt);

        return providerId;
    }

    public TokenResponse login(OAuthProviderPrincipal OAuthProviderPrincipal) {
        User user = userRepository.findByProviderIdAndProvider(OAuthProviderPrincipal.providerId(),
                        OAuthProviderPrincipal.provider())
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUND));
        TokenResponse response = userTokenProvider.createToken(user.getId());

        return response;
    }
}
