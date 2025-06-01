package com.chatting.capstone.domain.location.controller;

import com.chatting.capstone.domain.location.dto.request.CoordinateUpdateRequest;
import com.chatting.capstone.domain.location.service.CoordinateService;
import com.chatting.capstone.global.response.ApiResponse;
import com.chatting.capstone.global.response.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.chatting.capstone.global.response.ResponseStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coordinates")
public class CoordinateController {

    private final CoordinateService coordinateService;

    // 좌표 조회
    @GetMapping("/{nickname}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserCoordinate(@PathVariable String nickname) {
        Map<String, String> location = coordinateService.getUserLocation(nickname);
        if (location.isEmpty()) {
            throw new CustomException(ResponseStatus.COORDINATE_NOT_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.of(ResponseStatus.COORDINATE_SUCCESS, location));
    }

    // 좌표 갱신
    @PostMapping("/{nickname}")
    public ResponseEntity<ApiResponse<Void>> updateUserCoordinate(
            @PathVariable String nickname,
            @RequestBody CoordinateUpdateRequest request) {
        coordinateService.setUserLocation(nickname, request.getRoomName(), request.getX(), request.getY());
        return ResponseEntity.ok(ApiResponse.of(ResponseStatus.COORDINATE_SUCCESS));
    }
}
