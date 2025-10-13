package com.chatting.capstone.global.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiModerationResponse {
    private Output output;

    public Output getOutput() { return output; }
    public void setOutput(Output output) { this.output = output; }

    public static class Output {
        private String output;
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
    }
}
