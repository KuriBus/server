package com.chatting.capstone.domain.customization.controller;

import com.chatting.capstone.domain.customization.dto.request.CustomizationRequest;
import com.chatting.capstone.domain.customization.service.CustomizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customization")
public class CustomizationController {

    private final CustomizationService customizationService;

    public CustomizationController(CustomizationService service) {
        this.customizationService = service;
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCustomization(@RequestBody CustomizationRequest request) {
        customizationService.updateCustomization(request);
        return ResponseEntity.ok().build();
    }

}
