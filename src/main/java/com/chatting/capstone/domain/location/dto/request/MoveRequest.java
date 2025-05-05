package com.chatting.capstone.domain.location.dto.request;

import lombok.Data;

// 이동 요청 DTO
@Data
public class MoveRequest {
    private String userId;
    private String direction; // "w", "a", "s", "d"
}
