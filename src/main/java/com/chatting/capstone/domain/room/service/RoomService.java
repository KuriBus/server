package com.chatting.capstone.domain.room.service;

import com.chatting.capstone.domain.location.service.CoordinateService;
import com.chatting.capstone.domain.room.dto.response.RoomResponse;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.dto.response.UserResponse;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CoordinateService coordinateService;

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
    public void joinRoomByNickname(String nickname, Long roomId) {
        //전체 닉네임으로
        // 1. 사용자 조회
        User user = userRepository.findByNickname(nickname)
            .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        // 2. 입장할 방 조회
        Room targetRoom = roomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(ResponseStatus.ROOM_NOT_FOUND));

        // 3. 이미 해당 방에 있는지 확인
        if (user.getRoom() != null && user.getRoom().getId().equals(roomId)) {
            throw new CustomException(ResponseStatus.ALREADY_IN_ROOM);
        }

        // 4. 사용자에게 방 정보 설정
        user.setRoom(targetRoom);
        userRepository.save(user);

        // 5. 방의 중앙 좌표 계산
        int centerX = targetRoom.getWidth() / 2;
        int centerY = targetRoom.getHeight() / 2;

        // 6. Redis에 초기 위치 정보 저장/갱신
        coordinateService.setUserLocation(nickname, targetRoom.getRoomName(), centerX, centerY);
    }

    // 방 퇴장
    @Transactional
    public void  leaveRoom(Long roomId, String nickname) {
        //전체 닉네임으로
        // 1. 방 찾기
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(ResponseStatus.ROOM_NOT_FOUND));

        // 2. 유저 찾기
        User user = userRepository.findByNickname(nickname)
            .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        // 3. 유저가 현재 방에 속해 있는지 확인
        if (user.getRoom() == null || !user.getRoom().getId().equals(room.getId())) {
            throw new CustomException(ResponseStatus.USER_NOT_IN_ROOM);
        }

        // 4. 유저 방 정보 DB에서 제거
        user.setRoom(null);
        userRepository.save(user);

        // 5. Redis에서 사용자 위치 정보 삭제
        coordinateService.deleteUserLocation(nickname);
    }
}
