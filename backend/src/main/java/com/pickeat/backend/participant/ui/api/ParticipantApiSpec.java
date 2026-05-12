package com.pickeat.backend.participant.ui.api;

import com.pickeat.backend.global.argument.principal.ParticipantPrincipal;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequest;
import com.pickeat.backend.participant.application.dto.response.MyParticipantCodeResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "참여자 관리", description = "참여자 생성, 상태 관리 및 조회 API")
public interface ParticipantApiSpec {

    @Operation(
            summary = "참여자 생성 (픽잇 입장)",
            description = "닉네임과 픽잇 코드를 통해 해당 세션에 참여자로 등록하고 인증 토큰을 발급받습니다.",
            operationId = "createParticipant",
            requestBody = @RequestBody(
                    description = "참여자 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ParticipantRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "참여자 생성 및 토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 정보 (유효하지 않은 픽잇 코드 등)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 픽잇 세션을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    ResponseEntity<TokenResponse> createParticipant(
            @Valid @org.springframework.web.bind.annotation.RequestBody ParticipantRequest request
    );

    @Operation(
            summary = "본인 참여자 코드 조회",
            description = "현재 로그인한 사용자의 참여자 식별 코드를 조회합니다.",
            operationId = "getMyParticipantCode",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyParticipantCodeResponse.class))
            )
    })
    ResponseEntity<MyParticipantCodeResponse> getMyParticipantCode(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "투표 완료 표시",
            description = "본인의 식당 선택 및 투표가 완료되었음을 표시합니다.",
            operationId = "markCompletion",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "완료 처리 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    ResponseEntity<Void> markCompletion(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "투표 완료 취소",
            description = "표시했던 투표 완료 상태를 취소하고 수정 가능 상태로 변경합니다.",
            operationId = "cancelCompletion",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 처리 성공")
    })
    ResponseEntity<Void> cancelCompletion(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "전체 참여자 메타 정보 조회",
            description = "현재 픽잇 세션에 참여 중인 모든 사용자들의 기본 정보를 조회합니다.",
            operationId = "getAllParticipantMeta",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "참여자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ParticipantResponse[].class))
            )
    })
    ResponseEntity<List<ParticipantResponse>> getAllParticipantMeta(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "전체 참여자 상태 조회",
            description = "참여자들의 투표 완료 여부 등 전반적인 진행 상태를 조회합니다.",
            operationId = "getAllParticipantState",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "참여자 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = ParticipantStateResponse.class))
            )
    })
    ResponseEntity<ParticipantStateResponse> getAllParticipantState(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );
}
