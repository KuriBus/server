package com.chatting.capstone.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRequest {

    @Schema(description = "사용자 닉네임", example = "kuriverse")
    private String nickname;  //이것도 닉네임으로
}
