package com.chatting.capstone.domain.location.controller;

import com.chatting.capstone.domain.location.service.UserLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coordinates")
public class CoordinateController {

    private final UserLocationService userLocationService;

    // 특정 사용자의 현재 좌표 정보(방 이름 포함) 조회
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, String>> getUserCoordinate(@PathVariable  String userId) {
        Map<String, String> location = userLocationService.getUserLocation(userId);

        if (location.isEmpty()) {
            // Redis에 사용자 위치 정보가 없으면 404 반환
            return ResponseEntity.notFound().build();
        } else {
            // 정보가 있으면 200 OK 와 함께 Map 데이터 반환
            return ResponseEntity.ok(location);
        }
    }

    // POST, PATCH, GET (all users) 엔드포인트는 구현 필요, WebSocket에서 처리
}
