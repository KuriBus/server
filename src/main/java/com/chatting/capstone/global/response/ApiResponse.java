package com.chatting.capstone.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final int status;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data; // 필요할 때만 사용

    // data가 있는 응답
    public static <T> ApiResponse<T> of(ResponseStatus status, T data) {
        return new ApiResponse<>(status.getStatus().value(), status.getMessage(), data);
    }

    // data가 필요 없는 응답
    public static ApiResponse<Void> of(ResponseStatus status) {
        return new ApiResponse<>(status.getStatus().value(), status.getMessage(), null);
    }
}
