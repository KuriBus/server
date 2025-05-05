package com.chatting.capstone.domain.location.dto.request;

import lombok.Getter;
import lombok.Setter;

// 상호작용 요청 DTO
@Getter
@Setter
public class InteractionRequest {
    private String userId;
    private String key; // "e"
}
