package com.chatting.capstone.domain.user.service;

import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 로그인
    @Transactional
    public User loginOrCreate(String nickname, String ipAddress) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(ResponseStatus.NICKNAME_REQUIRED);
        }
        User user = userRepository.findByNickname(nickname)
                .map(existingUser -> { // 기존 사용자
                    if (existingUser.isActive()) {
                        throw new CustomException(ResponseStatus.NICKNAME_ALREADY_LOGGED_IN);
                    }
                    existingUser.setActive(true);
                    existingUser.setIpAddress(ipAddress);
                    existingUser.setRoom(null); // 방 정보는 여기서 설정 안 함
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> { // 새 사용자
                    User newUser = User.builder()
                            .nickname(nickname)
                            .ipAddress(ipAddress)
                            .active(true)
                            .room(null) // 초기 방 설정 안 함
                            .build();
                    return userRepository.save(newUser);
                });
        return user;
    }

    // 로그아웃
    @Transactional
    public void logout(User user) {
        if (user == null) {
            throw new CustomException(ResponseStatus.USER_NOT_FOUND);
        }

        user.setActive(false);
        user.setRoom(null); // 방에서 퇴장
        userRepository.save(user);
    }

    // 닉네임 중복 확인
    public void isNicknameTaken(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ResponseStatus.NICKNAME_TAKEN);
        }
        userRepository.existsByNickname(nickname);
    }
}
