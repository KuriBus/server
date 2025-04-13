package com.chatting.capstone.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor

public enum ResponseStatus {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    ALREADY_IN_ROOM(HttpStatus.CONFLICT, "이미 해당 방에 접속 중입니다."),
    USER_NOT_IN_ROOM(HttpStatus.BAD_REQUEST, "유저는 해당 방에 속해 있지 않습니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    NICKNAME_ALREADY_LOGGED_IN(HttpStatus.CONFLICT, "이미 로그인 중인 닉네임입니다."),
    ROOM_JOIN_SUCCESS(HttpStatus.OK, "방에 입장했습니다."),
    ROOM_LEAVE_SUCCESS(HttpStatus.OK, "방에서 퇴장했습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 되었습니다."),
    ALREADY_LOGGED_OUT(HttpStatus.BAD_REQUEST, "이미 로그아웃 상태입니다."),
    NICKNAME_TAKEN(HttpStatus.CONFLICT, "사용중인 닉네임입니다."),
    NICKNAME_AVAILABLE(HttpStatus.OK, "사용 가능한 닉네임입니다."),
    USER_ALREADY_LOGGED_IN(HttpStatus.CONFLICT, "이미 로그인한 유저입니다."),
    USER_CREATION_SUCCESS(HttpStatus.CREATED, "새로운 유저가 생성되었습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, "유저가 로그아웃 되었습니다.");

    private final HttpStatus status;
    private final String message;
}
