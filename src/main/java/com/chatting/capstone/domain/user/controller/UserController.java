package com.chatting.capstone.domain.user.controller;

import com.chatting.capstone.domain.user.dto.request.LoginRequest;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.service.UserService;
import com.chatting.capstone.global.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest,
            HttpSession session,
            HttpServletRequest request) {
        String nickname = loginRequest.getNickname();
        String ipAddress = IpUtil.getClientIP(request);

        try {
            User user = userService.loginOrCreate(nickname, ipAddress); // IP 전달
            session.setAttribute("user", user);
            return ResponseEntity.ok("로그인에 성공했습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            userService.logout(user);
            session.invalidate();
            return ResponseEntity.ok("로그아웃 되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("이미 로그아웃 상태입니다.");
        }
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<String> checkNickname(@RequestParam String nickname) {
        if (userService.isNicknameTaken(nickname)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("사용중인 닉네임입니다.");
        } else {
            return ResponseEntity.ok("사용 가능한 닉네임입니다.");
        }
    }
}
