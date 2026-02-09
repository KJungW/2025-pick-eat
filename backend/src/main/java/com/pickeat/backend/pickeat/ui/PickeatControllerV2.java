package com.pickeat.backend.pickeat.ui;

import com.pickeat.backend.global.auth.annotation.LoginUserId;
import com.pickeat.backend.global.log.BusinessLogging;
import com.pickeat.backend.pickeat.application.PickeatResultService;
import com.pickeat.backend.pickeat.application.PickeatService;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponseV2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PickeatControllerV2 {

    private final PickeatService pickeatService;
    private final PickeatResultService pickeatResultService;

    @PostMapping("/pickeats/new")
    public ResponseEntity<PickeatResponseV2> createPickeatWithoutRoomV2(@Valid @RequestBody PickeatRequest request) {
        PickeatResponseV2 response = pickeatService.createPickeatWithoutRoomV2(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @BusinessLogging("방에서 픽잇 생성")
    @PostMapping("/rooms/{roomId}/pickeats/new")
    public ResponseEntity<PickeatResponseV2> createPickeatWithRoomV2(
            @PathVariable("roomId") Long roomId,
            @LoginUserId Long userId,
            @Valid @RequestBody PickeatRequest request
    ) {
        PickeatResponseV2 response = pickeatService.createPickeatWithRoomV2(roomId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
