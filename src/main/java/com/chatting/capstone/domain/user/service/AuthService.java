package com.chatting.capstone.domain.user.service;

import com.chatting.capstone.domain.user.dto.response.LoginResponse;
import com.chatting.capstone.domain.user.dto.response.TokenResponse;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.config.JwtUtil;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import jakarta.transaction.Transactional;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    public LoginResponse login(String username, String password) {
        User user = userService.login(username, password);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        redisTemplate.opsForValue().set(
            "RT:" + user.getId(),
            refreshToken,
            Duration.ofDays(14)
        );

        return new LoginResponse(
            accessToken,
            refreshToken,
            user.getUsername(),
            user.getNickname()
        );
    }

    public void logout(User user) {
        redisTemplate.delete("RT:" + user.getId());
        userService.logout(user); // 사용자 상태 변경 (active=false, room=null)
    }


    @Transactional
    public TokenResponse reissueToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ResponseStatus.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtUtil.parseClaims(refreshToken).get("userId").toString();
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + userId);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ResponseStatus.REFRESH_TOKEN_MISMATCH);
        }

        User user = userRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        String newAccessToken = jwtUtil.generateAccessToken(user);

        return new TokenResponse(newAccessToken);
    }
}
