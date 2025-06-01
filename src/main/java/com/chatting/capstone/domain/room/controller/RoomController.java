package com.chatting.capstone.domain.room.controller;

import com.chatting.capstone.domain.room.dto.response.RoomResponse;
import com.chatting.capstone.domain.room.service.RoomService;
import com.chatting.capstone.domain.user.dto.request.UserRequest;
import com.chatting.capstone.global.response.ApiResponse;
import com.chatting.capstone.global.response.ResponseStatus;
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
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity
                .status(ResponseStatus.ROOM_LIST_SUCCESS.getStatus())
                .body(ApiResponse.of(ResponseStatus.ROOM_LIST_SUCCESS, rooms));
    }

    // 방 입장
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse> joinRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        roomService.joinRoomByNickname(request.getNickname(), roomId);
        return ResponseEntity
                .status(ResponseStatus.ROOM_JOIN_SUCCESS.getStatus())
                .body(ApiResponse.of(ResponseStatus.ROOM_JOIN_SUCCESS));
    }

    // 방 퇴장
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse> leaveRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        roomService.leaveRoom(roomId, request.getNickname());
        return ResponseEntity
                .status(ResponseStatus.ROOM_LEAVE_SUCCESS.getStatus())
                .body(ApiResponse.of(ResponseStatus.ROOM_LEAVE_SUCCESS));
    }
}
