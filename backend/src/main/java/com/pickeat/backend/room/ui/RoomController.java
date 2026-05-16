package com.pickeat.backend.room.ui;

import com.pickeat.backend.global.argument.annotation.User;
import com.pickeat.backend.global.argument.principal.UserPrincipal;
import com.pickeat.backend.global.log.aspect.UserTracingLogging;
import com.pickeat.backend.room.application.RoomService;
import com.pickeat.backend.room.application.dto.request.RoomInvitationRequest;
import com.pickeat.backend.room.application.dto.request.RoomRequest;
import com.pickeat.backend.room.application.dto.response.RoomResponse;
import com.pickeat.backend.room.ui.api.RoomApiSpec;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/rooms")
public class RoomController implements RoomApiSpec {

    private final RoomService roomService;

    @Override
    @PostMapping
    @UserTracingLogging(action = "방 생성")
    public ResponseEntity<RoomResponse> create(
            @Valid @RequestBody RoomRequest request,
            @User UserPrincipal userPrincipal
    ) {
        RoomResponse response = roomService.createRoom(request, userPrincipal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> get(
            @PathVariable("roomId") Long roomId,
            @User UserPrincipal userPrincipal
    ) {
        RoomResponse response = roomService.getRoom(roomId, userPrincipal.userId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAll(
            @User UserPrincipal userPrincipal
    ) {
        List<RoomResponse> response = roomService.getAllRoom(userPrincipal.userId());
        return ResponseEntity.ok(response);
    }

    @Override
    @UserTracingLogging(action = "방 초대")
    @PostMapping("/{roomId}/invite")
    public ResponseEntity<Void> invite(
            @PathVariable("roomId") Long roomId,
            @User UserPrincipal userPrincipal,
            @Valid @RequestBody RoomInvitationRequest request
    ) {
        roomService.inviteUsers(roomId, userPrincipal.userId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping("/{roomId}/exit")
    public ResponseEntity<Void> exit(
            @PathVariable("roomId") Long roomId,
            @User UserPrincipal userPrincipal
    ) {
        roomService.exitRoom(roomId, userPrincipal.userId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
