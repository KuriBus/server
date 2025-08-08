package com.chatting.capstone.global.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiModerationResponse {
    private String text;
    private double malice_score;
    @JsonProperty("is_harmful")
    private boolean isHarmful;
    private String confidence;   // 문자열 ("매우 위험", "위험", "보통" 등)
    private String purified_text;
}
