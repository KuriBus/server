package com.chatting.capstone.domain.chat.ai_ver;

import com.chatting.capstone.global.moderation.AiModerationResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@Service
public class ClovaXClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.moderation-url}")
    private String moderationUrl;

    public AiModerationResponse filterMessage(String text) {
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

            AiModerationResponse body = response.getBody();
            if (body == null) {
                return new AiModerationResponse(); // 실패 시 빈 객체
            }
            return body;
        } catch (Exception e) {
            log.error("ClovaXClient 호출 실패", e);
            AiModerationResponse fallback = new AiModerationResponse();
            fallback.setPurified_text(text); // 예외 시 원본 사용
            fallback.setMalice_score(0.0);
            return fallback;
        }
    }
}