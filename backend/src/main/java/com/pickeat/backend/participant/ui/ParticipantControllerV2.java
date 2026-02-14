package com.pickeat.backend.participant.ui;

import com.pickeat.backend.global.auth.annotation.ParticipantInPickeatV2;
import com.pickeat.backend.global.auth.principal.ParticipantPrincipalV2;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.ParticipantServiceV2;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequestV2;
import com.pickeat.backend.participant.application.dto.response.MyParticipantCodeResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponseV2;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponseV2;
import jakarta.validation.Valid;
import java.util.List;
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
    public ResponseEntity<MyParticipantCodeResponse> getMyParticipantCode(
            @ParticipantInPickeatV2 ParticipantPrincipalV2 principal
    ) {
        return ResponseEntity.ok(new MyParticipantCodeResponse(principal.participantCode()));
    }

    @PostMapping("/participants/me/completion/complete/new")
    public ResponseEntity<Void> markCompletion(
            @ParticipantInPickeatV2 ParticipantPrincipalV2 principal
    ) {
        participantService.markCompletion(principal.pickeatCode(), principal.participantCode());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/participants/me/completion/cancel/new")
    public ResponseEntity<Void> cancelCompletion(
            @ParticipantInPickeatV2 ParticipantPrincipalV2 principal
    ) {
        participantService.cancelCompletion(principal.pickeatCode(), principal.participantCode());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/participants/meta/new")
    public ResponseEntity<List<ParticipantResponseV2>> getAllParticipantMeta(
            @ParticipantInPickeatV2 ParticipantPrincipalV2 principal
    ) {
        List<ParticipantResponseV2> response = participantService.getMetaInPickeat(principal.pickeatCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/participants/state/new")
    public ResponseEntity<ParticipantStateResponseV2> getAllParticipantState(
            @ParticipantInPickeatV2 ParticipantPrincipalV2 principal
    ) {
        ParticipantStateResponseV2 response = participantService.getStateInPickeat(principal.pickeatCode());
        return ResponseEntity.ok(response);
    }
}
