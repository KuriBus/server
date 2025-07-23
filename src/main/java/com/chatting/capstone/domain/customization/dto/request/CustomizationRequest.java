package com.chatting.capstone.domain.customization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomizationRequest {

    @Schema(description = "사용자 닉네임", example = "kuriverse")
    private String nickname;

    @Schema(description = "사용자 번호 (예: 유저 ID)", example = "1")
    private int bodyType;
}
