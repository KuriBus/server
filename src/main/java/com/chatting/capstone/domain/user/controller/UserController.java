package com.chatting.capstone.domain.user.controller;

import com.chatting.capstone.domain.user.dto.request.LoginRequest;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.service.UserService;
import com.chatting.capstone.global.response.ApiResponse;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import com.chatting.capstone.global.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest,
            HttpSession session,
            HttpServletRequest request) {
        String nickname = loginRequest.getNickname();
        String ipAddress = IpUtil.getClientIP(request);
        User user = userService.loginOrCreate(nickname, ipAddress);

        session.setAttribute("user", user); // 세션에 사용자 정보 저장
        return ResponseEntity
                .status(ResponseStatus.LOGIN_SUCCESS.getStatus())
                .body(ApiResponse.of(ResponseStatus.LOGIN_SUCCESS));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userService.logout(user);
            session.invalidate();
            return ResponseEntity
                    .status(ResponseStatus.LOGOUT_SUCCESS.getStatus())
                    .body(ApiResponse.of(ResponseStatus.LOGOUT_SUCCESS));
        } else {
            throw new CustomException(ResponseStatus.ALREADY_LOGGED_OUT);
        }
    }

    // 중복 닉네임 체크
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse> checkNickname(@RequestParam String nickname) {
        userService.isNicknameTaken(nickname);
        return ResponseEntity
                .status(ResponseStatus.NICKNAME_AVAILABLE.getStatus())
                .body(ApiResponse.of(ResponseStatus.NICKNAME_AVAILABLE));
    }
}
