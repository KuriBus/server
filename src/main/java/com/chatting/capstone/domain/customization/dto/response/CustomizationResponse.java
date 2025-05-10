package com.chatting.capstone.domain.customization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomizationResponse {
    private Long userId;
    private int hairType;
    private int outfitType;
}
