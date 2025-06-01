package com.chatting.capstone.domain.location.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateUpdateRequest {
    private String roomName;
    private int x;
    private int y;
}
