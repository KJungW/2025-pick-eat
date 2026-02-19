package com.pickeat.backend.acceptance_test.piece.restaurant;

import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantExcludeRequest;
import com.pickeat.backend.restaurant.application.dto.request.TemplateRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.request.WishRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponse;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantStateResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import org.springframework.http.HttpStatus;

public class RestaurantPieceTest {

    public static void 위치_기반_식당_후보_생성(String pickeatCode, LocationRestaurantRequest request) {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v2/pickeats/{pickeatCode}/restaurants/location", pickeatCode)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    public static void 위시리스트_기반_식당_후보_생성(String pickeatCode, WishRestaurantRequest request) {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v2/pickeats/{pickeatCode}/restaurants/wish", pickeatCode)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    public static void 템플릿_기반_식당_후보_생성(String pickeatCode, TemplateRestaurantRequest request) {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v2/pickeats/{pickeatCode}/restaurants/template", pickeatCode)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    public static List<RestaurantResponse> 식당_메타데이터_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v2/pickeats/restaurants")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static RestaurantStateResponse 식당_상태_조회(String participantToken) {
        return RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .get("/api/v2/pickeats/restaurants/state")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RestaurantStateResponse.class);
    }

    public static void 식당_제외(String participantToken, RestaurantExcludeRequest request) {
        RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v2/restaurants/exclude")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    public static void 식당_좋아요(String participantToken, String restaurantCode) {
        RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .patch("/api/v2/restaurants/{restaurantCode}/like", restaurantCode)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    public static void 식당_좋아요_취소(String participantToken, String restaurantCode) {
        RestAssured
                .given().log().all()
                .header("Pickeat-Participant-Token", "Bearer " + participantToken)
                .when()
                .patch("/api/v2/restaurants/{restaurantCode}/unlike", restaurantCode)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
