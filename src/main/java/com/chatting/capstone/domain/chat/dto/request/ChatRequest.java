package com.chatting.capstone.domain.chat.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
    private Long roomId;
    private Long userId;
    private String nickname;
    private String content;
}
