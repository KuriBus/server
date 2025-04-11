package com.chatting.capstone.domain.user.service;

import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 닉네임 중복 확인
    public boolean isNicknameTaken(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 로그인
    public User loginOrCreate(String nickname, String ipAddress) {
        Optional<User> optionalUser = userRepository.findByNickname(nickname);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.isActive()) {
                throw new RuntimeException("이미 로그인 중인 닉네임입니다.");
            }

            user.setActive(true);
            user.setIpAddress(ipAddress); // IP 업데이트
            return userRepository.save(user);
        }

        // 새 유저 생성
        User newUser = User.builder()
                .nickname(nickname)
                .ipAddress(ipAddress)
                .active(true)
                .room(null)
                .build();
        return userRepository.save(newUser);
    }

    // 로그아웃
    public void logout(User user) {
        user.setActive(false);
        userRepository.save(user);
    }
}
