package com.chatting.capstone.global.response;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ResponseStatus responseStatus;

    public CustomException(ResponseStatus responseStatus) {
        super(responseStatus.getMessage());
        this.responseStatus = responseStatus;
    }
}
