package com.pickeat.backend.global.log.model;

public enum LogType {
    USER_TRACE,             // 유저 트레이싱 로그
    REQUEST,                // HTTP 요청 로그
    RESPONSE,               // HTTP 응답 로그
    SERVER_ERROR,           // 서버 내부 에로 로그
    CLIENT_ERROR,           // 클라이언트 에러 로그
    EXTERNAL_ERROR,         // 외부 API/인프라 연동 에러 로그
    ;
}
