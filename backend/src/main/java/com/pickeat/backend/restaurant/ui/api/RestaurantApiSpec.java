package com.pickeat.backend.restaurant.ui.api;

import com.pickeat.backend.global.argument.principal.ParticipantPrincipal;
import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantExcludeRequest;
import com.pickeat.backend.restaurant.application.dto.request.TemplateRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.WishRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponse;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantStateResponse;
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
import java.util.List;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "식당 관리", description = "식당 검색 생성, 조회, 좋아요 및 제외 API")
public interface RestaurantApiSpec {

    @Operation(
            summary = "위치 기반 식당 생성",
            description = "특정 위치 정보를 바탕으로 식당 목록을 검색하여 생성합니다.",
            operationId = "createRestaurantsByLocation",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = LocationRestaurantRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "식당 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 위치 정보",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<Void> createRestaurantsByLocation(
            @Parameter(description = "픽잇 코드") @PathVariable("pickeatCode") String pickeatCode,
            @Valid @org.springframework.web.bind.annotation.RequestBody LocationRestaurantRequest request
    );

    @Operation(
            summary = "위시리스트 기반 식당 생성",
            description = "사용자의 위시리스트 정보를 바탕으로 식당 목록을 생성합니다.",
            operationId = "createRestaurantsByWish"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "식당 생성 성공")
    })
    ResponseEntity<Void> createRestaurantsByWish(
            @Parameter(description = "픽잇 코드") @PathVariable("pickeatCode") String pickeatCode,
            @Valid @org.springframework.web.bind.annotation.RequestBody WishRestaurantRequest request
    );

    @Operation(
            summary = "템플릿 기반 식당 생성",
            description = "사전 정의된 템플릿을 바탕으로 식당 목록을 생성합니다.",
            operationId = "createRestaurantsByTemplate"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "식당 생성 성공")
    })
    ResponseEntity<Void> createRestaurantsByTemplate(
            @Parameter(description = "픽잇 코드") @PathVariable("pickeatCode") String pickeatCode,
            @Valid @org.springframework.web.bind.annotation.RequestBody TemplateRestaurantRequest request
    );

    @Operation(
            summary = "픽잇 내 식당 메타 정보 조회",
            description = "현재 참여 중인 픽잇의 모든 식당 정보를 조회합니다.",
            operationId = "getRestaurantMetaInPickeat",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RestaurantResponse[].class))
            )
    })
    ResponseEntity<List<RestaurantResponse>> getRestaurantMetaInPickeat(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "픽잇 내 식당 상태 조회",
            description = "현재 픽잇의 전반적인 식당 선택 상태를 조회합니다.",
            operationId = "getRestaurantStateInPickeat",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RestaurantStateResponse.class))
            )
    })
    ResponseEntity<RestaurantStateResponse> getRestaurantStateInPickeat(
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "식당 제외 처리",
            description = "선택된 식당들을 후보군에서 제외합니다.",
            operationId = "excludeRestaurants",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "제외 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 식당 코드 포함",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    value = "{\"title\": \"BAD_REQUEST\", \"detail\": \"존재하지 않는 식당 코드가 포함되어 있습니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<Void> excludeRestaurants(
            @org.springframework.web.bind.annotation.RequestBody RestaurantExcludeRequest request,
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "식당 좋아요",
            description = "특정 식당에 대해 좋아요를 표시합니다.",
            operationId = "likeRestaurant",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요 성공"),
            @ApiResponse(responseCode = "404", description = "식당을 찾을 수 없음")
    })
    ResponseEntity<Void> likeRestaurant(
            @Parameter(description = "식당 코드") @PathVariable("restaurantCode") String restaurantCode,
            @Parameter(hidden = true) ParticipantPrincipal principal
    );

    @Operation(
            summary = "식당 좋아요 취소",
            description = "표시했던 식당 좋아요를 취소합니다.",
            operationId = "cancelLikeRestaurant",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "ParticipantAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요 취소 성공")
    })
    ResponseEntity<Void> cancelLikeRestaurant(
            @Parameter(description = "식당 코드") @PathVariable("restaurantCode") String restaurantCode,
            @Parameter(hidden = true) ParticipantPrincipal principal
    );
}
