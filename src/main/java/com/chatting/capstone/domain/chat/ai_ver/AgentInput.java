package com.chatting.capstone.domain.chat.ai_ver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentInput {
    private InnerInput input;
    private Map<String, Object> config;

    public AgentInput(String text) {
        this.input = new InnerInput(text);
        this.config = Map.of(); // 빈 맵
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InnerInput {
        private String input;
    }
}
