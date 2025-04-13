package com.chatting.capstone.global.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        ResponseStatus status = ResponseStatus.valueOf(ex.getReason());
        return buildErrorResponse(status);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        return buildErrorResponse(ResponseStatus.SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(ResponseStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.getStatus().value()); // 상태 코드
        response.put("message", status.getMessage()); // 메시지
        return ResponseEntity.status(status.getStatus()).body(response);
    }

    public static ResponseEntity<Map<String, Object>> buildSuccessResponse(ResponseStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.getStatus().value()); // 상태 코드
        response.put("message", status.getMessage()); // 메시지
        return ResponseEntity.status(status.getStatus()).body(response);
    }
}
