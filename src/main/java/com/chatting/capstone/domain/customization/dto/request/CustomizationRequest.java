package com.chatting.capstone.domain.customization.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomizationRequest {
    private String nickname;
    private int hairType;
    private int outfitType;
}
