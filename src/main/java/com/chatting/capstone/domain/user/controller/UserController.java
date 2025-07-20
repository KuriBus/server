package com.chatting.capstone.domain.user.controller;

import com.chatting.capstone.domain.user.dto.request.LoginRequest;
import com.chatting.capstone.domain.user.dto.request.SignupRequest;
import com.chatting.capstone.domain.user.dto.response.LoginResponse;
import com.chatting.capstone.domain.user.dto.response.TokenResponse;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.service.AuthService;
import com.chatting.capstone.domain.user.service.UserService;
import com.chatting.capstone.global.response.ApiResponse;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final AuthService authService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody SignupRequest request, HttpServletRequest httpRequest) {
        String ipAddress = extractClientIp(httpRequest);
        userService.signup(request.getUsername(), request.getPassword(), request.getNickname(), ipAddress);
        return ResponseEntity
            .status(ResponseStatus.SIGNUP_SUCCESS.getStatus())
            .body(ApiResponse.of(ResponseStatus.SIGNUP_SUCCESS));
    }
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request.getUsername(), request.getPassword());

        // refreshToken을 HttpOnly 쿠키로 설정
        Cookie refreshCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // HTTPS 환경에서만 사용 local 사용시 false로
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 14일

        response.addCookie(refreshCookie);
        // accessToken만 ResponseBody로 내려주기 (refreshToken은 쿠키로만 전달)
        LoginResponse body = new LoginResponse(
            loginResponse.getAccessToken(),
            null,
            loginResponse.getUsername(),
            loginResponse.getNickname()
        );

        return ResponseEntity.ok(ApiResponse.of(ResponseStatus.LOGIN_SUCCESS, body));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            authService.logout(user);
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

    //ip 추출
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            // 여러 IP가 있을 수 있으므로 첫 번째 IP만 사용
            ip = ip.split(",")[0];
        }
        return ip;
    }

    //토큰 추출 및 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.of(ResponseStatus.INVALID_REFRESH_TOKEN, null));
        }

        TokenResponse tokenResponse = authService.reissueToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.of(ResponseStatus.REFRESH_TOKEN_COOKIE_SUCCESS, tokenResponse));
    }
}
