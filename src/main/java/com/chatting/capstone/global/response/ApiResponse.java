package com.chatting.capstone.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private final int status;
    private final String message;

    public static ApiResponse of(ResponseStatus status) {
        return new ApiResponse(status.getStatus().value(), status.getMessage());
    }
}
