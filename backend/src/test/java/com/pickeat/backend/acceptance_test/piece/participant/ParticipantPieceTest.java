package com.pickeat.backend.acceptance_test.piece.participant;

import static org.hamcrest.Matchers.notNullValue;

import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequestV2;
import com.pickeat.backend.participant.application.dto.response.MyParticipantCodeResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponseV2;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponseV2;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import org.springframework.http.HttpStatus;

public class ParticipantPieceTest {

    public static TokenResponse 참가자_생성(ParticipantRequestV2 request) {
        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/participants/new")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("token", notNullValue())
                .extract()
                .as(TokenResponse.class);
    }

    public static MyParticipantCodeResponse 자신의_참가자_코드_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/participants/me/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MyParticipantCodeResponse.class);
    }

    public static void 참가자_선택_완료_표시(String participantToken) {
        RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .patch("/api/v1/participants/me/completion/complete/new")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    public static void 참가자_선택_완료_표시_취소(String participantToken) {
        RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .patch("/api/v1/participants/me/completion/cancel/new")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    public static List<ParticipantResponseV2> 모든_참가자_메타데이터_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/participants/meta/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static ParticipantStateResponseV2 모든_참가자_상태_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v1/participants/state/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ParticipantStateResponseV2.class);
    }
}
