package com.chatting.capstone.domain.location.controller;

import com.chatting.capstone.domain.location.dto.request.MoveRequest;
import com.chatting.capstone.domain.location.dto.response.PositionResponse;
import com.chatting.capstone.domain.location.dto.request.PortalRequest;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import java.util.Arrays;
import java.util.Collection;
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

@Controller
@RequiredArgsConstructor
public class MovementController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    private static final List<String> PATH = Arrays.asList(
        "교실", "통로 1", "공원", "통로 2", "문화공간", "통로 3" //방 수정
    );

    @MessageMapping("/move")
    @SendTo("/topic/positions")
    public Collection<PositionResponse> move(MoveRequest message) {
        String nickname = message.getNickname();
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ResponseStatus.USER_NOT_FOUND);
        }
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        // User의 실제 방 정보
        String currentActualRoomName = (user.getRoom() != null) ? user.getRoom().getRoomName() : null;

        // Redis에서 위치 정보 조회 (nickname 기준)
        String redisKey = "user:location:" + nickname;
        Map<Object, Object> redisValue = redisTemplate.opsForHash().entries(redisKey);

        int x, y;
        try {
            x = redisValue.containsKey("x") ? Integer.parseInt((String) redisValue.get("x")) : 800;
            y = redisValue.containsKey("y") ? Integer.parseInt((String) redisValue.get("y")) : 450;
        } catch (NumberFormatException e) {
            throw new CustomException(ResponseStatus.INVALID_POSITION);
        }
        String roomName = currentActualRoomName;

        // "init"이면 기본 위치로 초기화
        if ("init".equals(message.getDirection())) {
            x = 20;
            y = 11;
        } else {
            // 방향 이동 처리
            switch (message.getDirection()) {
                case "w": y = Math.max(-800, y - 1); break;
                case "a": x = Math.max(-450, x - 1); break;
                case "s": y = Math.min(800, y + 1); break;
                case "d": x = Math.min(450, x + 1); break;
            }
        }

        // 위치 정보 Redis에 저장 (nickname 기준)
        Map<String, Object> saveValue = new HashMap<>();
        saveValue.put("roomName", roomName);
        saveValue.put("x", String.valueOf(x));
        saveValue.put("y", String.valueOf(y));
        redisTemplate.opsForHash().putAll(redisKey, saveValue);

        // 모든 유저의 위치 정보를 Redis에서 조회 (nickname 기준)
        List<PositionResponse> allPositions = userRepository.findAll().stream()
                .map(u -> {
                    String key = "user:location:" + u.getNickname();
                    Map<Object, Object> v = redisTemplate.opsForHash().entries(key);
                    if (v.isEmpty()) return null;
                    int px = v.containsKey("x") ? Integer.parseInt((String) v.get("x")) : 800;
                    int py = v.containsKey("y") ? Integer.parseInt((String) v.get("y")) : 450;
                    String pr = v.containsKey("roomName") ? (String) v.get("roomName") : null;
                    return new PositionResponse(u.getNickname(), px, py, pr);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return allPositions;
    }

    @MessageMapping("/portal")
    @SendTo("/topic/positions")
    public Collection<PositionResponse> portal(PortalRequest message) {
        String nickname = message.getNickname();

        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        String currentActualRoomName = (user.getRoom() != null) ? user.getRoom().getRoomName() : null;
        if (currentActualRoomName == null) {
            throw new CustomException(ResponseStatus.ROOM_NOT_FOUND);
        }

        // Redis에서 위치 정보 조회 (nickname 기준)
        String redisKey = "user:location:" + nickname;
        Map<Object, Object> redisValue = redisTemplate.opsForHash().entries(redisKey);

        int x, y;
        try {
            x = redisValue.containsKey("x") ? Integer.parseInt((String) redisValue.get("x")) : 800;
            y = redisValue.containsKey("y") ? Integer.parseInt((String) redisValue.get("y")) : 450;
        } catch (NumberFormatException e) {
            throw new CustomException(ResponseStatus.INVALID_POSITION);
        }

        int idx = PATH.indexOf(currentActualRoomName);
        if (idx == -1) {
            throw new CustomException(ResponseStatus.ROOM_NOT_FOUND);
        }

        int nextIdx = "left".equals(message.getPortalDirection())
                ? (idx - 1 + PATH.size()) % PATH.size()
                : (idx + 1) % PATH.size();
        String nextRoomName = PATH.get(nextIdx);

        // 다음 방 엔티티 조회 및 User.room 업데이트
        Room nextRoomEntity = roomRepository.findByRoomName(nextRoomName)
                .orElseThrow(() -> new CustomException(ResponseStatus.ROOM_NOT_FOUND));
        user.setRoom(nextRoomEntity);
        userRepository.save(user);

        // 포탈 이동 시 위치 설정
        x = "left".equals(message.getPortalDirection()) ? 8 : 1;
        y = 5;

        // 위치 정보 Redis에 저장 (nickname 기준)
        Map<String, Object> saveValue = new HashMap<>();
        saveValue.put("roomName", nextRoomName);
        saveValue.put("x", String.valueOf(x));
        saveValue.put("y", String.valueOf(y));
        redisTemplate.opsForHash().putAll(redisKey, saveValue);

        return getAllPositions();
    }

    // 모든 유저의 위치 정보를 Redis에서 조회 (nickname 기준)
    private List<PositionResponse> getAllPositions() {
        return userRepository.findAll().stream()
                .map(u -> {
                    String key = "user:location:" + u.getNickname();
                    Map<Object, Object> v = redisTemplate.opsForHash().entries(key);
                    if (v.isEmpty()) return null;
                    int px, py;
                    try {
                        px = v.containsKey("x") ? Integer.parseInt((String) v.get("x")) : 800;
                        py = v.containsKey("y") ? Integer.parseInt((String) v.get("y")) : 450;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    String pr = v.containsKey("roomName") ? (String) v.get("roomName") : null;
                    return new PositionResponse(u.getNickname(), px, py, pr);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
