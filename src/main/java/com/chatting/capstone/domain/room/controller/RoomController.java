package com.chatting.capstone.domain.room.controller;

import com.chatting.capstone.domain.room.dto.response.RoomResponse;
import com.chatting.capstone.domain.room.service.RoomService;
import com.chatting.capstone.domain.user.dto.request.UserRequest;
import java.util.List;
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

    //입장 퇴장 모두 닉네임 기반으로
    // 방 입장
    @PostMapping("/{roomId}/join")
    public ResponseEntity<String> joinRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        roomService.joinRoomByNickname(request.getNickname(), roomId);
        return ResponseEntity.ok( "방에 성공적으로 입장했습니다.");
    }

    // 방 퇴장
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<String> leaveRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        roomService.leaveRoom(roomId, request.getNickname());
        return ResponseEntity.ok("방에서 성공적으로 퇴장했습니다.");
    }
}
