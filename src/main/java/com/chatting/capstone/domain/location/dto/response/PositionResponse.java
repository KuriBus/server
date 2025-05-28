package com.chatting.capstone.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

// 위치 정보 응답 DTO
@Data
@AllArgsConstructor

public class PositionResponse {
    private String nickname;
    private int x;
    private int y;
    private String roomName;
}
