package com.pickeat.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Pickeat 관련 에러
    PROCESSING_PICKEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "진행중인 픽잇을 찾을 수 없습니다. 존재하지 않는 픽잇이거나 이미 종료된 픽잇입니다."),
    PICKEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "픽잇을 찾을 수 없습니다."),
    PICKEAT_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "픽잇 기록을 찾을 수 없습니다."),
    PICKEAT_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "픽잇 결과를 찾을 수 없습니다."),

    // Participant 관련 에러
    PARTICIPANT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 픽잇에 동일한 참가자가 생성되었습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "참가지 기록이 존재하지 않습니다."),
    PARTICIPANT_RESTAURANT_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 눌렀거나 올바르지 않은 식당입니다."),
    PARTICIPANT_RESTAURANT_NOT_LIKED(HttpStatus.BAD_REQUEST, "좋아요 기록이 없거나 올바르지 않은 식당입니다."),

    // Wish 관련 에러
    WISH_NOT_FOUND(HttpStatus.NOT_FOUND, "위시를 찾을 수 없습니다."),
    WISH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 위시에 접근할 권한이 없습니다."),

    // WishPicture 관련 에러
    NOT_ALLOWED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "허용하지 않은 위시 사진 타입입니다"),
    WISH_PICTURE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 위시 이미지에 접근할 권한이 없습니다."),

    // Restaurant 관련 에러
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "식당이 존재하지 않습니다."),
    RESTAURANT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 픽잇에 대한 식당이 생성되었습니다."),
    RESTAURANT_EXCLUDE_FAIL(HttpStatus.BAD_REQUEST, "식당 소거에 실패했습니다. 식당 코드를 다시 한번 확인해주세요."),

    // User 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    ALREADY_NICKNAME_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임 입니다."),
    SIGN_UP_REQUIRED(HttpStatus.UNAUTHORIZED, "회원가입이 필요한 계정입니다."),

    // Room 관련 에러
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "방을 찾을 없습니다."),
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 방에 접근할 권한이 없습니다."),
    ROOM_HAS_NO_WISHES(HttpStatus.BAD_REQUEST, "벙애 위시가 존재하지 않습니다."),

    // Template 관련 에러
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),

    //입력 검증 에러
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력 데이터 검증에 실패했습니다."),

    // 헤더 관련 에러
    INVALID_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "인증 헤더가 존재하지 않거나 올바르지 않습니다."),

    // Jwt 관련 에러
    TOKEN_IS_EMPTY(HttpStatus.FORBIDDEN, "인증되지 않은 사용자입니다."),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, "잘못된 인증 정보입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // Storage 관련 에러
    INVALID_STORAGE_KEY_ARGUMENT_COUNT(HttpStatus.INTERNAL_SERVER_ERROR, "Storage 키를 생성하기 위한 인자의 개수가 적절하지 않습니다."),
    INVALID_STORAGE_KEY_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "Storage 키를 생성하기 위한 인자 형식 적절하지 않습니다."),
    STORAGE_KEY_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Storage 키를 생성하는데 실패했습니다."),

    // 요청 형식 에러
    REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력 데이터 검증에 실패했습니다."),
    INVALID_HTTP_METHOD(HttpStatus.BAD_REQUEST, ""),


    // 외부 API 및 인프라 에러
    EXTERNAL_API_FAIL_5XX(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 연결에 실패했습니다."),
    EXTERNAL_API_FAIL_4XX(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 연결에 실패했습니다."),

    // 서버 내부 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
