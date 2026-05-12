package com.pickeat.backend.login.ui;

import com.pickeat.backend.global.argument.annotation.OAuthProvider;
import com.pickeat.backend.global.argument.principal.OAuthProviderPrincipal;
import com.pickeat.backend.login.application.LoginService;
import com.pickeat.backend.login.application.OAuthProviderTokenProvider;
import com.pickeat.backend.login.application.dto.request.AuthCodeRequest;
import com.pickeat.backend.login.application.dto.request.SignupRequest;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.login.ui.api.LoginApiSpec;
import com.pickeat.backend.user.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController implements LoginApiSpec {

    private final LoginService loginService;
    private final UserService userService;
    private final OAuthProviderTokenProvider OAuthProviderTokenProvider;

    @Override
    @PostMapping("/code")
    public ResponseEntity<TokenResponse> processCode(@Valid @RequestBody AuthCodeRequest request) {
        Long providerId = loginService.getProviderIdFromIdToken(request);

        TokenResponse response = OAuthProviderTokenProvider.createToken(providerId, request.provider());

        if (!userService.isUserExist(providerId, request.provider())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@OAuthProvider OAuthProviderPrincipal OAuthProviderPrincipal) {
        TokenResponse response = loginService.login(OAuthProviderPrincipal);

        return ResponseEntity.ok().body(response);
    }

    @Override
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody SignupRequest request,
            @OAuthProvider OAuthProviderPrincipal OAuthProviderPrincipal) {
        userService.createUser(request, OAuthProviderPrincipal);

        TokenResponse response = loginService.login(OAuthProviderPrincipal);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
