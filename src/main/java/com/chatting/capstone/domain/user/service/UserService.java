package com.chatting.capstone.domain.user.service;

import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.exception.ResponseStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // 닉네임 중복 확인
    public boolean isNicknameTaken(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 로그인
    @Transactional
    public User loginOrCreate(String nickname, String ipAddress) {
        Room randomRoom = getRandomRoom(); // 방 먼저 선택

        return userRepository.findByNickname(nickname)
                .map(user -> {
                    if (user.isActive()) {
                        throw new RuntimeException("이미 로그인 중인 닉네임입니다.");
                    }
                    user.setActive(true);
                    user.setIpAddress(ipAddress);
                    user.setRoom(randomRoom); // 방 재할당
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .nickname(nickname)
                            .ipAddress(ipAddress)
                            .active(true)
                            .room(randomRoom)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    // 방 랜덤 선택
    private Room getRandomRoom() {
        List<Room> rooms = roomRepository.findAll();

        if (rooms.isEmpty()) {
            throw new ResponseStatusException(ResponseStatus.ROOM_NOT_FOUND.getStatus(),
                    ResponseStatus.ROOM_NOT_FOUND.name());
        }

        Random random = new Random();
        return rooms.get(random.nextInt(rooms.size()));
    }

    // 로그아웃
    public void logout(User user) {
        if (user == null) {
            throw new ResponseStatusException(ResponseStatus.USER_NOT_FOUND.getStatus(),
                    ResponseStatus.USER_NOT_FOUND.name());
        }

        user.setActive(false);
        user.setRoom(null); // 방에서 퇴장
        userRepository.save(user);
    }
}
