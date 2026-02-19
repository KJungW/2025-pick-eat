package com.pickeat.backend.participant.ui;

import com.pickeat.backend.global.auth.annotation.ParticipantInPickeat;
import com.pickeat.backend.global.auth.principal.ParticipantPrincipal;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.ParticipantService;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequest;
import com.pickeat.backend.participant.application.dto.response.MyParticipantCodeResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponse;
import com.pickeat.backend.participant.ui.api.ParticipantApiSpec;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ParticipantController implements ParticipantApiSpec {

    private final ParticipantService participantService;

    @Override
    @PostMapping("/participants")
    public ResponseEntity<TokenResponse> createParticipant(
            @Valid @RequestBody ParticipantRequest request
    ) {
        TokenResponse response = participantService.createParticipant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/participants/me")
    public ResponseEntity<MyParticipantCodeResponse> getMyParticipantCode(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        return ResponseEntity.ok(new MyParticipantCodeResponse(principal.participantCode()));
    }

    @Override
    @PatchMapping("/participants/me/completion/complete")
    public ResponseEntity<Void> markCompletion(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        participantService.markCompletion(principal.pickeatCode(), principal.participantCode());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/participants/me/completion/cancel")
    public ResponseEntity<Void> cancelCompletion(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        participantService.cancelCompletion(principal.pickeatCode(), principal.participantCode());
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/participants/meta")
    public ResponseEntity<List<ParticipantResponse>> getAllParticipantMeta(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        List<ParticipantResponse> response = participantService.getMetaInPickeat(principal.pickeatCode());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/participants/state")
    public ResponseEntity<ParticipantStateResponse> getAllParticipantState(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        ParticipantStateResponse response = participantService.getStateInPickeat(principal.pickeatCode());
        return ResponseEntity.ok(response);
    }
}
