package com.pickeat.backend.acceptance_test.piece.pickeat;

import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.http.HttpStatus;

public class PickeatPieceTest {

    public static PickeatResponse 외부용_픽잇_생성(PickeatRequest request) {
        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/pickeats")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(PickeatResponse.class);
    }

    public static PickeatResponse 방_내부용_픽잇_생성(Long roomId, String accessToken, PickeatRequest request) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/rooms/{roomId}/pickeats", roomId)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(PickeatResponse.class);
    }

    public static void 픽잇_종료(String participantToken) {
        RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .post("/api/v1/pickeats/complete")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    public static PickeatResponse 픽잇_메타데이터_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/pickeats/meta")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PickeatResponse.class);
    }

    public static PickeatStateResponse 픽잇_상태_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/pickeats/state")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PickeatStateResponse.class);
    }

    public static PickeatResultResponse 픽잇_결과_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/pickeats/result")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PickeatResultResponse.class);
    }
}
