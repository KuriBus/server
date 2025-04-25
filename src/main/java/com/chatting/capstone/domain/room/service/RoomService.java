package com.chatting.capstone.domain.room.service;

import com.chatting.capstone.domain.location.service.UserLocationService;
import com.chatting.capstone.domain.room.dto.response.RoomResponse;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.dto.response.UserResponse;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.exception.ResponseStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserLocationService userLocationService;

    // 방 목록 조회
    public List<RoomResponse> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();

        return rooms.stream().map(room ->
                new RoomResponse(
                        room.getId(),
                        room.getRoomName(),
                        room.getUsers().stream()
                                .map(user -> new UserResponse(user.getNickname()))
                                .collect(Collectors.toList())
                )
        ).collect(Collectors.toList());
    }

    // 방 입장
    @Transactional
    public void joinRoomById(Long userId, Long roomId) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다. ID: " + userId));

        // 2. 입장할 방 조회
        Room targetRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "입장하려는 방을 찾을 수 없습니다. ID: " + roomId));

        // 3. 이미 해당 방에 있는지 확인
        if (user.getRoom() != null && user.getRoom().getId().equals(roomId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, // 409 Conflict
                    "이미 해당 방(" + targetRoom.getRoomName() + ")에 참여 중입니다.");
        }

        // 4. 사용자에게 방 정보 설정
        user.setRoom(targetRoom);
        userRepository.save(user); // 변경된 사용자 정보 저장

        // 5. 방의 중앙 좌표 계산
        int centerX = targetRoom.getWidth() / 2;  // 1600 / 2 = 800
        int centerY = targetRoom.getHeight() / 2; // 900 / 2 = 450

        // 7. Redis에 초기 위치 정보 저장/갱신
        // userId를 String으로 변환하여 사용 (UserLocationService 스펙에 맞춤)
        userLocationService.setUserLocation(String.valueOf(userId), targetRoom.getRoomName(), centerX, centerY);
    }

    // 방 퇴장
    @Transactional
    public void  leaveRoom(Long roomId, Long userId) {
        // 1. 방 찾기
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseStatus.ROOM_NOT_FOUND.getStatus(),
                        ResponseStatus.ROOM_NOT_FOUND.name()
                ));

        // 2. 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseStatus.USER_NOT_FOUND.getStatus(),
                        ResponseStatus.USER_NOT_FOUND.name()
                ));

        // 3. 유저가 현재 방에 속해 있는지 확인
        if (user.getRoom() == null || !user.getRoom().getId().equals(room.getId())) {
            throw new ResponseStatusException(
                    ResponseStatus.USER_NOT_IN_ROOM.getStatus(),
                    ResponseStatus.USER_NOT_IN_ROOM.name()
            );
        }

        // 4. 유저 방 정보 DB에서 제거
        user.setRoom(null);
        userRepository.save(user);

        // 5. Redis에서 사용자 위치 정보 삭제
        // userId를 String으로 변환하여 전달 (UserLocationService 스펙 확인)
        userLocationService.deleteUserLocation(String.valueOf(userId));
    }
}
