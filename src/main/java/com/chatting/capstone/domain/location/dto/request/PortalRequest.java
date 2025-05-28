package com.chatting.capstone.domain.location.dto.request;

import lombok.Getter;
import lombok.Setter;

// 포탈 이동 요청 DTO
@Getter
@Setter
public class PortalRequest {
    private String nickname;
    private String portalDirection; // "left" 또는 "right"
}
