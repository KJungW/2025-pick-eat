package com.pickeat.backend.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ClientErrorCode implements ErrorCode {

    // - Pickeat 관련 에러
    PICKEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "픽잇을 찾을 수 없습니다. 존재하지 않는 픽잇이거나 이미 종료된 픽잇입니다."),
    PICKEAT_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "픽잇 기록을 찾을 수 없습니다."),
    PICKEAT_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "픽잇 결과를 찾을 수 없습니다."),

    // - Participant 관련 에러
    PARTICIPANT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 픽잇에 동일한 참가자가 생성되었습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "참가지 기록이 존재하지 않습니다."),
    PARTICIPANT_RESTAURANT_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 눌렀거나 올바르지 않은 식당입니다."),
    PARTICIPANT_RESTAURANT_NOT_LIKED(HttpStatus.BAD_REQUEST, "좋아요 기록이 없거나 올바르지 않은 식당입니다."),

    // - Wish 관련 에러
    WISH_NOT_FOUND(HttpStatus.NOT_FOUND, "위시를 찾을 수 없습니다."),
    WISH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 위시에 접근할 권한이 없습니다."),

    // - WishPicture 관련 에러
    NOT_ALLOWED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "허용하지 않은 위시 사진 타입입니다"),
    WISH_PICTURE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 위시 이미지에 접근할 권한이 없습니다."),

    // - Restaurant 관련 에러
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "식당이 존재하지 않습니다."),
    RESTAURANT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 픽잇에 대한 식당이 생성되었습니다."),
    RESTAURANT_EXCLUDE_FAIL(HttpStatus.BAD_REQUEST, "식당 소거에 실패했습니다. 식당 코드를 다시 한번 확인해주세요."),

    // - User 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    ALREADY_NICKNAME_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임 입니다."),
    SIGN_UP_REQUIRED(HttpStatus.UNAUTHORIZED, "회원가입이 필요한 계정입니다."),

    // - Room 관련 에러
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "방을 찾을 없습니다."),
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 방에 접근할 권한이 없습니다."),
    ROOM_HAS_NO_WISHES(HttpStatus.BAD_REQUEST, "벙애 위시가 존재하지 않습니다."),

    // - Template 관련 에러
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),

    // - 인증 관련 에러
    INVALID_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "인증 헤더가 존재하지 않거나 올바르지 않습니다."),
    TOKEN_IS_EMPTY(HttpStatus.FORBIDDEN, "인증되지 않은 사용자입니다."),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, "잘못된 인증 정보입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // 요청 형식 에러
    REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 데이터의 형식이 올바르지 않거나 필드 타입이 일치하지 않습니다."),
    INVALID_HTTP_PROTOCOL(HttpStatus.BAD_REQUEST, "요청하신 URL 경로, HTTP 메서드 또는 미디어 타입이 유효하지 않습니다."),
    MULTIPART_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "파일 업로드 요청 규격이 올바르지 않거나 필수 파일 데이터가 누락되었습니다.");

    private final HttpStatus status;
    private final String message;

    ClientErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
