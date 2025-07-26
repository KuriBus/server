package com.chatting.capstone.domain.location.controller;

import com.chatting.capstone.domain.location.dto.request.CoordinateUpdateRequest;
import com.chatting.capstone.domain.location.service.CoordinateService;
import com.chatting.capstone.global.response.ApiResponse;
import com.chatting.capstone.global.response.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(
            summary = "유저 좌표 조회",
            description = "특정 유저의 좌표 정보를 조회합니다.",
            parameters = {
                    @Parameter(name = "nickname", description = "유저 닉네임", required = true, example = "kuriverse")
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌표 조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "좌표 정보 없음"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/{nickname}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserCoordinate(@PathVariable String nickname) {
        Map<String, Object> location = coordinateService.getUserLocation(nickname);
        if (location.isEmpty()) {
            throw new CustomException(ResponseStatus.COORDINATE_NOT_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.of(ResponseStatus.COORDINATE_SUCCESS, location));
    }

    // 좌표 갱신
    @Operation(
            summary = "유저 좌표 갱신",
            description = "특정 유저의 좌표를 갱신합니다.",
            parameters = {
                    @Parameter(name = "nickname", description = "유저 닉네임", required = true, example = "kuriverse")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "갱신할 좌표 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌표 갱신 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/{nickname}")
    public ResponseEntity<ApiResponse<Void>> updateUserCoordinate(
            @PathVariable String nickname,
            @RequestBody CoordinateUpdateRequest request) {
        coordinateService.setUserLocation(nickname, request.getRoomName(), request.getX(), request.getY());
        return ResponseEntity.ok(ApiResponse.of(ResponseStatus.COORDINATE_SUCCESS));
    }
}
