package com.chatting.capstone.domain.location.service;

import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate; // WebSocket 브로드캐스트용
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovementService {

    private final UserLocationService userLocationService; // Redis 서비스
    private final RoomRepository roomRepository;          // DB Room 정보
    private final SimpMessagingTemplate messagingTemplate;

    // WASD 이동 요청 처리 (좌표 제한 포함)
    public void handleMovement(String nickname, String key) {
        // 1. Redis에서 현재 위치 정보 가져오기
        Map<String, String> location = userLocationService.getUserLocation(nickname);
        if (location.isEmpty()) {
            return; // 위치 정보 없으면 이동 불가
        }

        String currentRoomName = location.get("roomName");
        int currentX = 0;
        int currentY = 0;
        try {
            currentX = Integer.parseInt(location.getOrDefault("x", "0"));
            currentY = Integer.parseInt(location.getOrDefault("y", "0"));
        } catch (NumberFormatException e) {
            return; // 좌표 파싱 실패 시 이동 불가
        }

        // 2. 현재 방 정보 가져오기 (DB 조회 - 캐싱 고려)
        Optional<Room> roomOpt = roomRepository.findByRoomName(currentRoomName);
        if (roomOpt.isEmpty()) {
            // 필요 시 사용자에게 오류 알림 로직 추가
            return; // 방 정보 없으면 이동 불가
        }
        Room currentRoom = roomOpt.get();

        // 3. 새 좌표 계산
        int newX = currentX;
        int newY = currentY;

        switch (key.toUpperCase()) {
            case "W":
                // 좌표 제한 로직: 0 이상
                newY = Math.max(0, currentY - 1);
                break;
            case "A":
                // 좌표 제한 로직: 0 이상
                newX = Math.max(0, currentX - 1);
                break;
            case "S":
                // 좌표 제한 로직: 방 높이(height) - 1 이하
                newY = Math.min(currentRoom.getHeight() - 1, currentY + 1);
                break;
            case "D":
                // 좌표 제한 로직: 방 너비(width) - 1 이하
                newX = Math.min(currentRoom.getWidth() - 1, currentX + 1);
                break;
            default:
                return; // WASD 아니면 무시
        }

        // 4. 좌표가 실제로 변경되었는지 확인
        if (newX != currentX || newY != currentY) {
            // 4.1 Redis 업데이트
            userLocationService.setUserLocation(nickname, currentRoomName, newX, newY);

            // 4.2 같은 방 사용자들에게 이동 결과 브로드캐스트 (다음 이슈에서 구현)

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

    // handleInteraction 메소드는 다른 이슈에서 구현
}
