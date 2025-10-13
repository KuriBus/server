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

    @Value("${ai.agent-url}") // ai.agent-url 사용
    private String agentUrl;

    public AiModerationResponse filterMessage(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //input 안에 input이 이중 구조라 이렇게 매핑
        Map<String, Object> payload = Map.of(
            "input", Map.of("input", text),
            "config", Map.of()
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            // 반환 타입은 그대로 AiModerationResponse
            ResponseEntity<AiModerationResponse> response = restTemplate.postForEntity(
                agentUrl,
                request,
                AiModerationResponse.class
            );

            AiModerationResponse body = response.getBody();
            if (body == null || body.getOutput() == null) {
                AiModerationResponse fallback = new AiModerationResponse();
                AiModerationResponse.Output out = new AiModerationResponse.Output(); // 기본 생성자 사용
                out.setOutput(text); // setter로 값 넣기
                fallback.setOutput(out);
                return fallback;
            }
            return body;
        } catch (Exception e) {
            log.error("ClovaXClient 호출 실패, 원본 텍스트 사용", e);
            AiModerationResponse fallback = new AiModerationResponse();
            AiModerationResponse.Output out = new AiModerationResponse.Output(); // 기본 생성자
            out.setOutput(text); // 값 세팅
            fallback.setOutput(out);
            return fallback;
        }
    }
}