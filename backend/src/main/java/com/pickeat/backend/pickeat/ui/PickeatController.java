package com.pickeat.backend.pickeat.ui;

import com.pickeat.backend.global.auth.annotation.LoginUserId;
import com.pickeat.backend.global.auth.annotation.ParticipantInPickeat;
import com.pickeat.backend.global.auth.principal.ParticipantPrincipal;
import com.pickeat.backend.global.log.BusinessLogging;
import com.pickeat.backend.pickeat.application.PickeatResultService;
import com.pickeat.backend.pickeat.application.PickeatService;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponse;
import com.pickeat.backend.pickeat.ui.api.PickeatApiSpec;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PickeatController implements PickeatApiSpec {

    private final PickeatService pickeatService;
    private final PickeatResultService pickeatResultService;

    @Override
    @PostMapping("/pickeats")
    public ResponseEntity<PickeatResponse> createPickeatWithoutRoom(@Valid @RequestBody PickeatRequest request) {
        PickeatResponse response = pickeatService.createPickeatWithoutRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @BusinessLogging("방에서 픽잇 생성")
    @PostMapping("/rooms/{roomId}/pickeats")
    public ResponseEntity<PickeatResponse> createPickeatWithRoom(
            @PathVariable("roomId") Long roomId,
            @LoginUserId Long userId,
            @Valid @RequestBody PickeatRequest request
    ) {
        PickeatResponse response = pickeatService.createPickeatWithRoom(roomId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/pickeats/complete")
    public ResponseEntity<Void> completePickeat(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        pickeatService.completePickeat(principal.pickeatCode());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @GetMapping("/pickeats/meta")
    public ResponseEntity<PickeatResponse> getPickeatMeta(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        PickeatResponse response = pickeatService.getPickeatMeta(principal.pickeatCode());
        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/pickeats/state")
    public ResponseEntity<PickeatStateResponse> getPickeatState(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        PickeatStateResponse response = pickeatService.getPickeatState(principal.pickeatCode());
        return ResponseEntity.ok().body(response);
    }

    @Override
    @GetMapping("/pickeats/result")
    public ResponseEntity<PickeatResultResponse> getPickeatResult(
            @ParticipantInPickeat ParticipantPrincipal principal
    ) {
        PickeatResultResponse response = pickeatResultService.getByPickeatCode(principal.pickeatCode());
        return ResponseEntity.ok().body(response);
    }
}
