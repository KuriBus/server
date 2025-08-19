package com.chatting.capstone.global.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiModerationService {

    private final RestTemplate restTemplate;

    @Value("${ai.moderation-url}")
    private String moderationUrl;

    public AiModerationResponse moderateText(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("text", text);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<AiModerationResponse> response = restTemplate.postForEntity(
                    moderationUrl,
                    request,
                    AiModerationResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("AI 서버 호출 실패", e);
        }
    }
}
