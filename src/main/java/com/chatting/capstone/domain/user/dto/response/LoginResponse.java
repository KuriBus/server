package com.chatting.capstone.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;      // JWT 토큰
    private String nickname;   // 게임에서 보여줄 이름
}
