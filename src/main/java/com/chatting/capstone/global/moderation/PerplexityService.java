/*
package com.chatting.capstone.global.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerplexityService {

    @Value("${api.perplexity.url}")
    private String perplexityApiUrl;

    @Value("${api.perplexity.key}")
    private String perplexityApiKey;

    private final WebClient webClient;
    private final ClovaService clovaService;

    */
/**
     * Transform a negative sentence into a positive one
     * @param sentence The sentence to transform
     * @return The positive version of the sentence
     *//*

    public String transformToPositive(String sentence) {
        // First, check if the sentence is negative using Clova
        String clovaResult = clovaService.appraiseSentence(sentence);

        // If Clova returns "1", it means the sentence is negative
        if (clovaResult != null && clovaResult.equals("1")) {
            log.info("Negative sentence detected: {}", sentence);
            return callPerplexityApi(sentence);
        } else {
            log.info("Sentence is already positive or neutral: {}", sentence);
            return sentence;
        }
    }

    private String callPerplexityApi(String sentence) {
        // Prepare the request body in the specified format
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "sonar");

        // Messages setup
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Be precise and concise.");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "이 부정적인 문장을 긍정적인 문장으로 변환해주세요. 이때 문장의 의미를 유지하면서 변환해주세요 "
            + "부가적인 설명은 필요하지 않으며, 필터링된 문장만 출력해 주세요. 예시로 병신이라고 하면 멍청이라고 한다던지 이런식으로" + sentence);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 150);
        requestBody.put("temperature", 0.2); // Adjusted temperature as per example
        requestBody.put("top_p", 0.9);

        // Search domain filter setup
        */
/*List<String> searchDomainFilter = new ArrayList<>();
        searchDomainFilter.add("<any>");
        requestBody.put("search_domain_filter", null);

        requestBody.put("return_images", false);
        requestBody.put("return_related_questions", false);
        requestBody.put("search_recency_filter", "<string>");
        requestBody.put("top_k", 0);
        requestBody.put("stream", false);
        requestBody.put("presence_penalty", 0);
        requestBody.put("frequency_penalty", 1);

        // Empty response format
        requestBody.put("response_format", null);

        // Web search options setup
        Map<String, String> webSearchOptions = new HashMap<>();
        webSearchOptions.put("search_context_size", "high");
        requestBody.put("web_search_options", webSearchOptions);*//*


        requestBody.remove("search_domain_filter");
        requestBody.remove("response_format");

        log.info("Sending request to Perplexity API: {}", requestBody);

        return webClient.post()
                .uri(perplexityApiUrl + "/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + perplexityApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractResponseText)
                .onErrorResume(e -> {
                    log.error("Error calling Perplexity API: {}", e.getMessage());
                    log.error("Request for sentence: {}", sentence);
                    return Mono.just("Perplexity API 호출 중 오류가 발생했습니다.");
                })
                .block();
    }

    private String extractResponseText(Map<String, Object> response) {
        try {
            if (response == null) {
                return "No response received from Perplexity API";
            }

            log.info("Received response from Perplexity API: {}", response);

            Object choices = response.get("choices");
            if (choices instanceof List) {
                List<?> choicesList = (List<?>) choices;
                if (!choicesList.isEmpty()) {
                    Object firstChoice = choicesList.get(0);
                    if (firstChoice instanceof Map) {
                        Map<String, Object> choiceMap = (Map<String, Object>) firstChoice;
                        Map<String, Object> message = (Map<String, Object>) choiceMap.get("message");
                        if (message != null && message.get("content") != null) {
                            return (String) message.get("content");
                        }
                    }
                }
            }

            log.warn("Unexpected response structure: {}", response);
            return "Perplexity API 응답으로부터 텍스트를 추출할 수 없습니다.";
        } catch (Exception e) {
            log.error("Error extracting text from response: {}", e.getMessage(), e);
            return "Perplexity API 응답 파싱 중 오류가 발생했습니다.";
        }
    }
}
*/
