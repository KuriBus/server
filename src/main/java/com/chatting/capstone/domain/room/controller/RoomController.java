package com.chatting.capstone.domain.room.controller;

import com.chatting.capstone.domain.room.dto.response.RoomResponse;
import com.chatting.capstone.domain.room.service.RoomService;
import com.chatting.capstone.domain.user.dto.request.UserRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    // 방 목록 조회
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    // 방 입장
    @PostMapping("/{roomId}/join")
    public ResponseEntity<Map<String, Object>> joinRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        return roomService.joinRoom(roomId, request.getUserId());
    }

    // 방 퇴장
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Map<String, Object>> leaveRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        return roomService.leaveRoom(roomId, request.getUserId());
    }
}
