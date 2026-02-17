package com.pickeat.backend.acceptance_test.piece.pickeat;

import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponseV2;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponseV2;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponseV2;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.http.HttpStatus;

public class PickeatPieceTest {

    public static PickeatResponseV2 외부용_픽잇_생성(PickeatRequest request) {
        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/pickeats/new")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(PickeatResponseV2.class);
    }

    public static PickeatResponseV2 방_내부용_픽잇_생성(Long roomId, String accessToken, PickeatRequest request) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/rooms/{roomId}/pickeats/new", roomId)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(PickeatResponseV2.class);
    }

    public static void 픽잇_종료(String participantToken) {
        RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .post("/api/v1/pickeats/complete/new")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    public static PickeatResponseV2 픽잇_메타데이터_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/pickeats/meta/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PickeatResponseV2.class);
    }

    public static PickeatStateResponseV2 픽잇_상태_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/pickeats/state/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PickeatStateResponseV2.class);
    }

    public static PickeatResultResponseV2 픽잇_결과_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/pickeats/result/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PickeatResultResponseV2.class);
    }
}
