package com.chatting.capstone.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

// 위치 정보 응답 DTO
@Data
@AllArgsConstructor
public class PositionResponse {
    private String userId;
    private int x;
    private int y;
    private String roomName;
}
