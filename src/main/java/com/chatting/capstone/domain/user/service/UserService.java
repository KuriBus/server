package com.chatting.capstone.domain.user.service;

import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 8;

    // 허용 문자: 한글, 영어, 숫자만 (공백, 특수문자 금지) 빈문자 허용X
    private static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9]+$";
    // 아이디 패턴
    private static final String USERNAME_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{4,16}$";
    // 비밀번호 패턴
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$";
    //아이디 비밀번호 4글자 이상 같은 문자 허용 X
    private static final int MIN_CONSECUTIVE_SAME_LENGTH = 4;

    // 예시 금지 단어 목록 (더 많은 욕설 포함 가능)
    // 추후에 욕설이 들어간다면 ai 모델로 검사하는 것은 어떠한지
    private static final List<String> BANNED_WORDS = List.of(
        "fuck", "shit", "좆", "씨발", "병신", "fuckyou", "개새", "ㅅㅂ", "ㅂㅅ", "시발", "장애인"
    );

    // 회원가입
    @Transactional
    public void signup(String username, String rawPassword, String nickname, String ipAddress) {
        if (userRepository.existsByUsername(username)) {
            throw new CustomException(ResponseStatus.USERNAME_TAKEN);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ResponseStatus.NICKNAME_TAKEN);
        }

        validateUsername(username);
        validatePassword(rawPassword);
        validatePasswordNotSimilarToUsername(username, rawPassword);
        validateNickname(nickname);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
            .username(username)
            .password(encodedPassword)
            .nickname(nickname)
            .ipAddress(ipAddress)
            .active(false)
            .build();
        userRepository.save(user);
    }

    // 로그인
    @Transactional
    public User login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(ResponseStatus.LOGIN_FAILED));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ResponseStatus.LOGIN_FAILED);
        }

        user.setActive(true); // 상태 업데이트 (선택)
        return userRepository.save(user);
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

    private void validateUsername(String username) {
        if (!username.matches(USERNAME_PATTERN)) {
            throw new CustomException(ResponseStatus.INVALID_USERNAME_FORMAT);
        }
    }

    private void validatePassword(String password) {
        if (!password.matches(PASSWORD_PATTERN)) {
            throw new CustomException(ResponseStatus.INVALID_PASSWORD_FORMAT);
        }
    }

    private void validatePasswordNotSimilarToUsername(String username, String password) {
        // username과 password에서 연속으로 MIN_CONSECUTIVE_SAME_LENGTH 이상 겹치는 부분이 있는지 검사
        int maxCheckLength = Math.min(username.length(), password.length());

        for (int length = MIN_CONSECUTIVE_SAME_LENGTH; length <= maxCheckLength; length++) {
            for (int i = 0; i <= username.length() - length; i++) {
                String sub = username.substring(i, i + length);
                if (password.contains(sub)) {
                    throw new CustomException(ResponseStatus.PASSWORD_TOO_SIMILAR_TO_USERNAME);
                }
            }
        }
    }
}
