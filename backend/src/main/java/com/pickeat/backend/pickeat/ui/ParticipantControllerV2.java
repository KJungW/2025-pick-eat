package com.pickeat.backend.pickeat.ui;

import com.pickeat.backend.global.auth.annotation.ParticipantInPickeatV2;
import com.pickeat.backend.global.auth.principal.ParticipantPrincipalV2;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.pickeat.application.ParticipantServiceV2;
import com.pickeat.backend.pickeat.application.dto.request.ParticipantRequestV2;
import com.pickeat.backend.pickeat.application.dto.response.ParticipantResponseV2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ParticipantControllerV2 {

    private final ParticipantServiceV2 participantService;

    @PostMapping("/participants/new")
    public ResponseEntity<TokenResponse> createParticipant(
            @Valid @RequestBody ParticipantRequestV2 request
    ) {
        TokenResponse response = participantService.createParticipant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/participants/me/new")
    public ResponseEntity<ParticipantResponseV2> getParticipant(
            @ParticipantInPickeatV2 ParticipantPrincipalV2 principal
    ) {
        ParticipantResponseV2 response = participantService.getParticipant(
                principal.pickeatCode(), principal.participantCode());
        return ResponseEntity.ok(response);
    }
}
