package com.chatting.capstone.domain.location.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateUpdateRequest {

    @Schema(description = "방 이름", example = "roomA")
    private String roomName;

    @Schema(description = "X 좌표", example = "10")
    private int x;

    @Schema(description = "Y 좌표", example = "20")
    private int y;
}
