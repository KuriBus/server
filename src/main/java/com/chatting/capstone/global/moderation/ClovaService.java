package com.chatting.capstone.global.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaService {

    @Value("${api.clova.base-url}")
    private String clovaBaseUrl;

    @Value("${api.clova.task-id}")
    private String clovaTaskId;

    @Value("${api.clova.key}")
    private String clovaApiKey;

    private final WebClient webClient;

    /**
     * Send a request to CLOVA API
     * @param sentence The input prompt for the AI
     * @return The API response as a String
     */
    public String appraiseSentence(String sentence) {
        String requestId = UUID.randomUUID().toString().substring(0, 10);
        String endpoint = String.format("/testapp/v1/tasks/%s/search", clovaTaskId);

        var requestBody = Map.of(
                "includeAiFilters", true,
                "text", sentence
        );

        return webClient.post()
                .uri(clovaBaseUrl + endpoint)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + clovaApiKey)
                .header("X-NCP-CLOVASTUDIO-REQUEST-ID", requestId)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractResponseText)
                .onErrorResume(e -> {
                    log.error("Error calling CLOVA API: {}", e.getMessage());
                    return Mono.just("CLOVA API 호출 중 오류가 발생했습니다.");
                })
                .block();
    }

    private String extractResponseText(Map<String, Object> response) {
        try {
            // Adjust this based on the actual response structure
            Map<String, Object> result = (Map<String, Object>) response.get("result");

            if (result != null) {
                return (String) result.get("outputText");
            }
            return "CLOVA의 응답으로부터 텍스트를 추출할 수 없습니다.";
        } catch (Exception e) {
            log.error("Error extracting text from response: {}", e.getMessage());
            return "CLOVA의 응답 파싱 중 오류가 발생했습니다.";
        }
    }
}