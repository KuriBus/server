package com.chatting.capstone.domain.room.controller;

import com.chatting.capstone.domain.room.dto.response.RoomResponse;
import com.chatting.capstone.domain.room.service.RoomService;
import com.chatting.capstone.domain.user.dto.request.UserRequest;
import com.chatting.capstone.global.response.ApiResponse;
import com.chatting.capstone.global.response.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(
        summary = "방 목록 조회",
        description = "현재 생성되어 있는 방 목록을 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "방 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
        }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity
            .status(ResponseStatus.ROOM_LIST_SUCCESS.getStatus())
            .body(ApiResponse.of(ResponseStatus.ROOM_LIST_SUCCESS, rooms));
    }

    // 방 입장
    @Operation(
        summary = "방 입장",
        description = "사용자가 특정 방에 입장합니다.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "roomId", description = "입장할 방의 ID", required = true, example = "1")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "입장할 사용자의 닉네임 정보",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserRequest.class)
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "방 입장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
        }
    )
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse> joinRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        roomService.joinRoomByNickname(request.getNickname(), roomId);
        return ResponseEntity
            .status(ResponseStatus.ROOM_JOIN_SUCCESS.getStatus())
            .body(ApiResponse.of(ResponseStatus.ROOM_JOIN_SUCCESS));
    }

    // 방 퇴장
    @Operation(
        summary = "방 퇴장",
        description = "사용자가 특정 방에서 퇴장합니다.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "roomId", description = "퇴장할 방의 ID", required = true, example = "1")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "퇴장할 사용자의 닉네임 정보",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserRequest.class)
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "방 퇴장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
        }
    )
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse> leaveRoom(@PathVariable Long roomId, @RequestBody UserRequest request) {
        roomService.leaveRoom(roomId, request.getNickname());
        return ResponseEntity
            .status(ResponseStatus.ROOM_LEAVE_SUCCESS.getStatus())
            .body(ApiResponse.of(ResponseStatus.ROOM_LEAVE_SUCCESS));
    }
}
