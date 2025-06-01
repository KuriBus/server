package com.chatting.capstone.domain.location.service;

import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate; // WebSocket 브로드캐스트용
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementService {

    private final CoordinateService coordinateService;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // WASD 이동 요청 처리 (좌표 제한 포함)
    public void handleMovement(String nickname, String key) {
        // 1. Redis에서 현재 위치 정보 가져오기
        Map<String, Object> location = coordinateService.getUserLocation(nickname);
        if (location.isEmpty()) {
            throw new CustomException(ResponseStatus.LOCATION_NOT_FOUND); // 위치 정보 없으면 이동 불가
        }

        String currentRoomName = (String) location.get("roomName");
        int currentX, currentY;
        try {
            currentX = Integer.parseInt((String) location.getOrDefault("x", "0"));
            currentY = Integer.parseInt((String) location.getOrDefault("y", "0"));
        } catch (NumberFormatException e) {
            throw new CustomException(ResponseStatus.INVALID_POSITION); // 좌표 파싱 실패 시 이동 불가
        }

        // 2. 현재 방 정보 가져오기 (DB 조회 - 캐싱 고려)
        Room currentRoom = roomRepository.findByRoomName(currentRoomName)
                .orElseThrow(() -> new CustomException(ResponseStatus.ROOM_NOT_FOUND)); // 방 정보 없으면 이동 불가

        // 3. 새 좌표 계산
        int newX = currentX, newY = currentY;

        switch (key.toUpperCase()) {
            case "W": newY = Math.max(0, currentY - 1); break; // 좌표 제한 로직: 0 이상
            case "A": newX = Math.max(0, currentX - 1); break; // 좌표 제한 로직: 0 이상
            case "S": newY = Math.min(currentRoom.getHeight() - 1, currentY + 1); break; // 좌표 제한 로직: 방 높이(height) - 1 이하
            case "D": newX = Math.min(currentRoom.getWidth() - 1, currentX + 1); break; // 좌표 제한 로직: 방 너비(width) - 1 이하
            default: throw new CustomException(ResponseStatus.INVALID_POSITION); // WASD 아니면 무시
        }

        // 4. 좌표가 실제로 변경되었는지 확인
        if (newX != currentX || newY != currentY) {
            // 4.1 Redis 업데이트
            coordinateService.setUserLocation(nickname, currentRoomName, newX, newY);

            // 4.2 같은 방 사용자들에게 이동 결과 브로드캐스트
            String destination = "/topic/room/" + currentRoomName + "/move";
            Map<String, Object> payload = Map.of(
                    "type", "MOVE",
                    "nickname", nickname,
                    "x", newX,
                    "y", newY
            );
            messagingTemplate.convertAndSend(destination, payload);
        }
    }
}
