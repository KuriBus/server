package com.chatting.capstone.domain.customization.controller;

import com.chatting.capstone.domain.customization.dto.request.CustomizationRequest;
import com.chatting.capstone.domain.customization.dto.response.CustomizationResponse;
import com.chatting.capstone.domain.customization.service.CustomizationService;
import com.chatting.capstone.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chatting.capstone.global.response.ResponseStatus;

@RestController
@RequestMapping("/api/customization")
public class CustomizationController {

    private final CustomizationService customizationService;

    public CustomizationController(CustomizationService service) {
        this.customizationService = service;
    }

    //커스터마이징 수정
    @Operation(
        summary = "커스터마이징 수정",
        description = "사용자의 기본 커스터마이징을 수정합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 커스터마이징 정보 (닉네임, 번호 포함)",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CustomizationRequest.class)
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "커스터마이징 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 사용자 또는 커스터마이징"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
        }
    )
    @PostMapping("/update")
    public ResponseEntity<ApiResponse> updateCustomization(@RequestBody CustomizationRequest request) {
        customizationService.updateCustomization(request);
        return ResponseEntity
            .status(ResponseStatus.CUSTOMIZATION_UPDATE_SUCCESS.getStatus())
            .body(ApiResponse.of(ResponseStatus.CUSTOMIZATION_UPDATE_SUCCESS));
    }

    //커스터마이징 조회
    @Operation(
        summary = "커스터마이징 조회",
        description = "등록된 모든 사용자의 커스터마이징 정보를 조회합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "커스터마이징 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
        }
    )
    @GetMapping("/all")
    public ResponseEntity<List<CustomizationResponse>> getAllCustomizations() {
        List<CustomizationResponse> responses = customizationService.getAllCustomizations();
        return ResponseEntity.ok(responses);
    }
}
