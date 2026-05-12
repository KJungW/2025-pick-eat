package com.pickeat.backend.wish.ui;

import com.pickeat.backend.global.argument.annotation.User;
import com.pickeat.backend.global.argument.principal.UserPrincipal;
import com.pickeat.backend.global.log.BusinessLogging;
import com.pickeat.backend.wish.application.WishService;
import com.pickeat.backend.wish.application.dto.request.WishRequest;
import com.pickeat.backend.wish.application.dto.response.WishResponse;
import com.pickeat.backend.wish.ui.api.WishApiSpec;
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
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class WishController implements WishApiSpec {

    private final WishService wishService;

    @Override
    @BusinessLogging("위시 생성")
    @PostMapping(value = "/rooms/{roomId}/wishes")
    public ResponseEntity<WishResponse> createWish(
            @PathVariable("roomId") Long roomId,
            @Valid @RequestBody WishRequest request,
            @User UserPrincipal userPrincipal
    ) {
        WishResponse wishResponse = wishService.createWish(roomId, request, userPrincipal.userId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(wishResponse);
    }

    @Override
    @BusinessLogging("위시 삭제")
    @DeleteMapping("/wishes/{wishId}")
    public ResponseEntity<Void> deleteWish(
            @PathVariable("wishId") Long wishId,
            @User UserPrincipal userPrincipal
    ) {
        wishService.deleteWish(wishId, userPrincipal.userId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/rooms/{roomId}/wishes")
    public ResponseEntity<List<WishResponse>> getWishesInRoom(
            @PathVariable("roomId") Long roomId,
            @User UserPrincipal userPrincipal
    ) {
        List<WishResponse> wishes = wishService.getWishes(roomId, userPrincipal.userId());
        return ResponseEntity.ok(wishes);
    }
}
