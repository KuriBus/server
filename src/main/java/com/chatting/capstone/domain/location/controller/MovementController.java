package com.chatting.capstone.domain.location.controller;

import com.chatting.capstone.domain.location.dto.request.MoveRequest;
import com.chatting.capstone.domain.location.dto.response.PositionResponse;
import com.chatting.capstone.domain.location.dto.request.PortalRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MovementController {
    private Map<String, PositionResponse> positions = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final List<String> PATH = Arrays.asList(
            "Room 1", "Bridge 1", "Room 2", "Bridge 2", "Room 3", "Bridge 3"
    );

    @MessageMapping("/move")
    @SendTo("/topic/positions")
    public Collection<PositionResponse> move(MoveRequest message) {
        String redisKey = "user:location:" + message.getUserId();

        // Redis에서 현재 roomName을 가져옴, 없으면 Room 1
        String roomName = (String) redisTemplate.opsForHash().get(redisKey, "roomName");
        if (roomName == null) roomName = "Room 1";

        PositionResponse pos = positions.get(message.getUserId());

        if (pos == null || "init".equals(message.getDirection())) {
            // 중앙값으로 초기화
            pos = new PositionResponse(message.getUserId(), 20, 11, roomName);
        } else {
            // 기존 이동 처리
            switch (message.getDirection()) {
                case "w": pos.setY(pos.getY() - 1); break;
                case "a": pos.setX(pos.getX() - 1); break;
                case "s": pos.setY(pos.getY() + 1); break;
                case "d": pos.setX(pos.getX() + 1); break;
            }
        }
        pos.setRoomName(roomName); // 항상 최신 roomName을 세팅

        positions.put(message.getUserId(), pos);

        // Redis에 위치 저장 (roomName을 항상 현재 값으로)
        Map<String, String> redisValue = new HashMap<>();
        redisValue.put("roomName", roomName);
        redisValue.put("x", String.valueOf(pos.getX()));
        redisValue.put("y", String.valueOf(pos.getY()));
        redisTemplate.opsForHash().putAll(redisKey, redisValue);

        return positions.values();
    }

    @MessageMapping("/portal")
    @SendTo("/topic/positions")
    public Collection<PositionResponse> portal(PortalRequest message) {
        String redisKey = "user:location:" + message.getUserId();
        String currentRoom = (String) redisTemplate.opsForHash().get(redisKey, "roomName");
        if (currentRoom == null) currentRoom = "Room 1";

        PositionResponse pos = positions.getOrDefault(
                message.getUserId(),
                new PositionResponse(message.getUserId(), 4, 5, currentRoom)
        );

        int idx = PATH.indexOf(currentRoom);
        int nextIdx;
        if ("left".equals(message.getPortalDirection())) {
            nextIdx = (idx - 1 + PATH.size()) % PATH.size();
        } else {
            nextIdx = (idx + 1) % PATH.size();
        }
        String nextRoom = PATH.get(nextIdx);

        if ("left".equals(message.getPortalDirection())) {
            pos.setX(8); // 오른쪽에서 들어왔으니 왼쪽 포탈로 나감
        } else {
            pos.setX(1); // 왼쪽에서 들어왔으니 오른쪽 포탈로 나감
        }
        pos.setY(5);
        pos.setRoomName(nextRoom);

        positions.put(message.getUserId(), pos);

        Map<String, String> redisValue = new HashMap<>();
        redisValue.put("roomName", nextRoom);
        redisValue.put("x", String.valueOf(pos.getX()));
        redisValue.put("y", String.valueOf(pos.getY()));
        redisTemplate.opsForHash().putAll(redisKey, redisValue);

        return positions.values();
    }
}
