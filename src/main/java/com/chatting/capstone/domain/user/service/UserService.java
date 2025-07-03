package com.chatting.capstone.domain.user.service;

import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 6;

    // 허용 문자: 한글, 영어, 숫자만 (공백, 특수문자 금지) 빈문자 허용X
    private static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9]+$";

    // 예시 금지 단어 목록 (더 많은 욕설 포함 가능)
    // 추후에 욕설이 들어간다면 ai 모델로 검사하는 것은 어떠한지
    private static final List<String> BANNED_WORDS = List.of(
        "fuck", "shit", "좆", "씨발", "병신", "fuckyou", "개새", "ㅅㅂ", "ㅂㅅ", "시발", "장애인"
    );

    // 로그인
    @Transactional
    public User loginOrCreate(String nickname, String ipAddress) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(ResponseStatus.NICKNAME_REQUIRED);
        }

        validateNickname(nickname); // 여기서 유효성 검사 실행

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

    private void validateNickname(String nickname) {
        if (nickname.length() < MIN_NICKNAME_LENGTH || nickname.length() > MAX_NICKNAME_LENGTH) {
            throw new CustomException(ResponseStatus.INVALID_NICKNAME_LENGTH);
        }

        if (!nickname.matches(NICKNAME_PATTERN)) {
            throw new CustomException(ResponseStatus.INVALID_NICKNAME_FORMAT);
        }

        for (String banned : BANNED_WORDS) {
            if (nickname.toLowerCase().contains(banned)) {
                throw new CustomException(ResponseStatus.PROFANE_NICKNAME);
            }
        }
    }
}
