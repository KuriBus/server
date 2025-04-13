package com.chatting.capstone.domain.room.service;

import com.chatting.capstone.domain.room.dto.response.RoomResponse;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.dto.response.UserResponse;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.exception.GlobalExceptionHandler;
import com.chatting.capstone.global.exception.ResponseStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

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
    public ResponseEntity<Map<String, Object>> joinRoom(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseStatus.ROOM_NOT_FOUND.getStatus(),
                        ResponseStatus.ROOM_NOT_FOUND.name()
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseStatus.USER_NOT_FOUND.getStatus(),
                        ResponseStatus.USER_NOT_FOUND.name()
                ));

        if (user.getRoom() != null && user.getRoom().getId().equals(roomId)) {
            throw new ResponseStatusException(
                    ResponseStatus.ALREADY_IN_ROOM.getStatus(),
                    ResponseStatus.ALREADY_IN_ROOM.name()
            );
        }

        user.setRoom(room);
        userRepository.save(user);
        return GlobalExceptionHandler.buildSuccessResponse(ResponseStatus.ROOM_JOIN_SUCCESS);
    }

    // 방 퇴장
    public ResponseEntity<Map<String, Object>> leaveRoom(Long roomId, Long userId) {
        // 방 찾기
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseStatus.ROOM_NOT_FOUND.getStatus(),
                        ResponseStatus.ROOM_NOT_FOUND.name()
                ));

        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        ResponseStatus.USER_NOT_FOUND.getStatus(),
                        ResponseStatus.USER_NOT_FOUND.name()
                ));

        // 유저가 해당 방에 속해 있는지 확인
        if (user.getRoom() == null || !user.getRoom().getId().equals(room.getId())) {
            // 예외 처리
            throw new ResponseStatusException(
                    ResponseStatus.USER_NOT_IN_ROOM.getStatus(),
                    ResponseStatus.USER_NOT_IN_ROOM.name()
            );
        }

        // 유저 방에서 퇴장
        user.setRoom(null);
        userRepository.save(user);

        // 퇴장 완료 메시지
        return GlobalExceptionHandler.buildSuccessResponse(ResponseStatus.ROOM_LEAVE_SUCCESS);
    }
}
