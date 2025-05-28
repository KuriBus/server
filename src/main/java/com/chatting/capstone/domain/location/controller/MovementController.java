package com.chatting.capstone.domain.location.controller;

import com.chatting.capstone.domain.location.dto.request.MoveRequest;
import com.chatting.capstone.domain.location.dto.response.PositionResponse;
import com.chatting.capstone.domain.location.dto.request.PortalRequest;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
public class MovementController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    private static final List<String> PATH = Arrays.asList(
        "Room 1", "Bridge 1", "Room 2", "Bridge 2", "Room 3", "Bridge 3"
    );

    @MessageMapping("/move")
    @SendTo("/topic/positions")
    @Transactional(readOnly = true)
    public Collection<PositionResponse> move(MoveRequest message) {
        String nickname = message.getNickname();
        if (nickname == null || nickname.isBlank()) {
            return Collections.emptyList();
        }
        User user = userRepository.findByNickname(nickname).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }
        // User의 실제 방 정보
        String currentActualRoomName = (user.getRoom() != null) ? user.getRoom().getRoomName() : null;

        // Redis에서 위치 정보 조회
        String redisKey = "user:display_location:" + user.getId();
        Map<Object, Object> redisValue = redisTemplate.opsForHash().entries(redisKey);

        int x = redisValue.containsKey("x") ? Integer.parseInt((String) redisValue.get("x")) : 20;
        int y = redisValue.containsKey("y") ? Integer.parseInt((String) redisValue.get("y")) : 11;
        String roomName = currentActualRoomName;

        // "init"이면 기본 위치로 초기화
        if ("init".equals(message.getDirection())) {
            x = 20;
            y = 11;
        } else {
            // 방향 이동 처리
            switch (message.getDirection()) {
                case "w": y = Math.max(0, y - 1); break;
                case "a": x = Math.max(0, x - 1); break;
                case "s": y = Math.min(21, y + 1); break;
                case "d": x = Math.min(39, x + 1); break;
            }
        }

        // 위치 정보 Redis에 저장
        Map<String, String> saveValue = new HashMap<>();
        saveValue.put("roomName", roomName);
        saveValue.put("x", String.valueOf(x));
        saveValue.put("y", String.valueOf(y));
        redisTemplate.opsForHash().putAll(redisKey, saveValue);

        // 모든 유저의 위치 정보를 Redis에서 조회
        List<PositionResponse> allPositions = userRepository.findAll().stream()
            .map(u -> {
                String key = "user:display_location:" + u.getId();
                Map<Object, Object> v = redisTemplate.opsForHash().entries(key);
                if (v.isEmpty()) return null;
                int px = v.containsKey("x") ? Integer.parseInt((String) v.get("x")) : 20;
                int py = v.containsKey("y") ? Integer.parseInt((String) v.get("y")) : 11;
                String pr = v.containsKey("roomName") ? (String) v.get("roomName") : null;
                return new PositionResponse(u.getNickname(), px, py, pr);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return allPositions;
    }

    @MessageMapping("/portal")
    @SendTo("/topic/positions")
    @Transactional
    public Collection<PositionResponse> portal(PortalRequest message) {
        String nickname = message.getNickname();

        User user = userRepository.findByNickname(nickname).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }

        String currentActualRoomName = (user.getRoom() != null) ? user.getRoom().getRoomName() : null;
        if (currentActualRoomName == null) {
            return getAllPositions();
        }

        // Redis에서 위치 정보 조회
        String redisKey = "user:display_location:" + user.getId();
        Map<Object, Object> redisValue = redisTemplate.opsForHash().entries(redisKey);

        int x = redisValue.containsKey("x") ? Integer.parseInt((String) redisValue.get("x")) : 20;
        int y = redisValue.containsKey("y") ? Integer.parseInt((String) redisValue.get("y")) : 11;

        int idx = PATH.indexOf(currentActualRoomName);
        if (idx == -1) {
            return getAllPositions();
        }

        int nextIdx = "left".equals(message.getPortalDirection())
            ? (idx - 1 + PATH.size()) % PATH.size()
            : (idx + 1) % PATH.size();
        String nextRoomName = PATH.get(nextIdx);

        // 다음 방 엔티티 조회 및 User.room 업데이트
        Room nextRoomEntity = roomRepository.findByRoomName(nextRoomName)
            .orElseThrow(() -> new RuntimeException("다음 방을 찾을 수 없습니다: " + nextRoomName));
        user.setRoom(nextRoomEntity);
        userRepository.save(user);

        // 포탈 이동 시 위치 설정
        x = "left".equals(message.getPortalDirection()) ? 8 : 1;
        y = 5;

        // 위치 정보 Redis에 저장
        Map<String, String> saveValue = new HashMap<>();
        saveValue.put("roomName", nextRoomName);
        saveValue.put("x", String.valueOf(x));
        saveValue.put("y", String.valueOf(y));
        redisTemplate.opsForHash().putAll(redisKey, saveValue);

        return getAllPositions();
    }

    // 모든 유저의 위치 정보를 Redis에서 조회
    private List<PositionResponse> getAllPositions() {
        return userRepository.findAll().stream()
            .map(u -> {
                String key = "user:display_location:" + u.getId();
                Map<Object, Object> v = redisTemplate.opsForHash().entries(key);
                if (v.isEmpty()) return null;
                int px = v.containsKey("x") ? Integer.parseInt((String) v.get("x")) : 20;
                int py = v.containsKey("y") ? Integer.parseInt((String) v.get("y")) : 11;
                String pr = v.containsKey("roomName") ? (String) v.get("roomName") : null;
                return new PositionResponse(u.getNickname(), px, py, pr);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
