package com.chatting.capstone.global.response;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException ex) {
        ResponseStatus status = ex.getResponseStatus();
        return ResponseEntity.status(status.getStatus()).body(ApiResponse.of(status));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {
        return ResponseEntity.status(ResponseStatus.SERVER_ERROR.getStatus())
                .body(ApiResponse.of(ResponseStatus.SERVER_ERROR));
    }
}
