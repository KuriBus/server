package com.chatting.capstone.domain.chat.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChatResponse {
    private Long id;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
}
