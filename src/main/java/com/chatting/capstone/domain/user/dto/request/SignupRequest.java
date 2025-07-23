package com.chatting.capstone.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class SignupRequest {

    @Schema(description = "아이디", example = "kuriverse123")
    private String username;

    @Schema(description = "비밀번호", example = "kyonggi123*")
    private String password;

    @Schema(description = "닉네임", example = "kuriverse")
    private String nickname;

}
