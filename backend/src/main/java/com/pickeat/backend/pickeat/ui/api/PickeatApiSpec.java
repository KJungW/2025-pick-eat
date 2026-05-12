package com.pickeat.backend.pickeat.ui.api;

import com.pickeat.backend.global.argument.principal.ParticipantPrincipal;
import com.pickeat.backend.global.argument.principal.UserPrincipal;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "픽잇 관리", description = "픽잇 생성, 상태 관리 및 결과 조회 API")
public interface PickeatApiSpec {

    @Operation(
            summary = "방 없이 픽잇 생성",
            description = "방에 소속되지 않은 독립적인 픽잇 세션을 생성합니다.",
            operationId = "createPickeatWithoutRoom",
            requestBody = @RequestBody(
                    description = "픽잇 생성 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PickeatRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "픽잇 생성 성공",
                    content = @Content(schema = @Schema(implementation = PickeatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    ResponseEntity<PickeatResponse> createPickeatWithoutRoom(
            @Valid @org.springframework.web.bind.annotation.RequestBody PickeatRequest request
    );

    @Operation(
            summary = "방에서 픽잇 생성",
            description = "특정 방 내에서 새로운 픽잇 세션을 생성합니다.",
            operationId = "createPickeatWithRoom",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "UserAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "방 내 픽잇 생성 성공",
                    content = @Content(schema = @Schema(implementation = PickeatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (방 멤버가 아님)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 방",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    ResponseEntity<PickeatResponse> createPickeatWithRoom(
            @Parameter(description = "방 ID") @PathVariable("roomId") Long roomId,
            @Parameter(hidden = true) UserPrincipal userId,
            @Valid @org.springframework.web.bind.annotation.RequestBody PickeatRequest request
    );

    @Operation(
            summary = "픽잇 종료",
            description = "현재 진행 중인 픽잇을 수동으로 종료하고 결과를 확정합니다.",
            operationId = "completePickeat",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "픽잇 종료 성공"),
            @ApiResponse(
                    responseCode = "403",
                    description = "종료 권한 없음 (방장/생성자 아님)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    ResponseEntity<Void> completePickeat(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "픽잇 메타 정보 조회",
            description = "참여 중인 픽잇의 기본 설정 및 메타 정보를 조회합니다.",
            operationId = "getPickeatMeta",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메타 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = PickeatResponse.class))
            )
    })
    ResponseEntity<PickeatResponse> getPickeatMeta(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "픽잇 진행 상태 조회",
            description = "현재 참여자들의 투표 현황 및 픽잇의 진행 상태를 조회합니다.",
            operationId = "getPickeatState",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "진행 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = PickeatStateResponse.class))
            )
    })
    ResponseEntity<PickeatStateResponse> getPickeatState(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "픽잇 최종 결과 조회",
            description = "종료된 픽잇의 최종 선정 결과를 조회합니다.",
            operationId = "getPickeatResult",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "결과 조회 성공",
                    content = @Content(schema = @Schema(implementation = PickeatResultResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "아직 종료되지 않은 픽잇",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "미종료 상태",
                                    value = "{\"title\": \"INVALID_STATE\", \"detail\": \"결과를 조회하기 위해선 픽잇이 종료되어야 합니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<PickeatResultResponse> getPickeatResult(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );
}
