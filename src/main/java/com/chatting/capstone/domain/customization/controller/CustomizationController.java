package com.chatting.capstone.domain.customization.controller;

import com.chatting.capstone.domain.customization.dto.request.CustomizationRequest;
import com.chatting.capstone.domain.customization.service.CustomizationService;
import com.chatting.capstone.global.response.ApiResponse;
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

    @PostMapping("/update")
    public ResponseEntity<ApiResponse> updateCustomization(@RequestBody CustomizationRequest request) {
        customizationService.updateCustomization(request);
        return ResponseEntity
                .status(ResponseStatus.CUSTOMIZATION_UPDATE_SUCCESS.getStatus())
                .body(ApiResponse.of(ResponseStatus.CUSTOMIZATION_UPDATE_SUCCESS));
    }
}
