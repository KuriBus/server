package com.chatting.capstone.domain.chat.ai_ver;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class ClovaXClient {

    private static final Logger logger = LoggerFactory.getLogger(ClovaXClient.class);
    private final WebClient webClient;
    private final String apiKey;
    private final String taskId;

    // 생성자: WebClient 초기화 및 API 키 설정
    public ClovaXClient(@Value("${CLOVA_API_KEY}") String apiKey,
                        @Value("${CLVOA_TASK_ID}") String taskId) {
        this.apiKey = apiKey;
        this.taskId = taskId;
        this.webClient = WebClient.builder()
            .baseUrl("https://clovastudio.stream.ntruss.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE) // 💡 JSON으로 응답 받기
            .build();
    }

    // 1. 욕설 여부 판단 (튜닝된 모델 사용)
    @Async
    public CompletableFuture<Double> getToxicityScoreAsync(String userMessage) {
        Map<String, Object> requestBody = Map.of(
            "inputs", Map.of("text", userMessage)
        );

        return webClient.post()
            .uri("/infer/v1/tuning/{taskId}", taskId)
            .header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString())
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(ToxicityResponse.class)
            .map(response -> response.getResult().getScore())
            .onErrorResume(ex -> {
                logger.error("Toxicity 분석 실패. 기본값 0.0 반환. reason={}", ex.getMessage());
                return Mono.just(0.0);
            })
            .toFuture();
    }

    // 2. 순화 요청 필요 시 호출
    private CompletableFuture<String> requestClovaFilteredMessage(String userMessage) {
        String prompt = "다음 문장에서 공격적이거나 불쾌감을 줄 수 있는 표현을 순화해줘. 순화된 문장만 돌려줘. 원문: \"" + userMessage + "\"";

        Map<String, Object> requestBody = Map.of(
            "messages", List.of(
                Map.of("role", "system", "content", prompt)
            ),
            "topP", 0.8,
            "topK", 0,
            "maxTokens", 256,
            "temperature", 0.5,
            "repetitionPenalty", 1.1,
            "stop", List.of(),
            "includeAiFilters", true,
            "seed", 0
        );

        //perplexity 사용하는걸로 바꾸기
        return webClient.post()
            .uri("/testapp/v3/chat-completions/HCX-005")
            .header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString())
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(ClovaXResponse.class)
            .map(response -> response.getResult().getMessage().getContent())
            .onErrorResume(ex -> {
                logger.error("순화 요청 실패. 원문 그대로 반환. reason={}", ex.getMessage());
                return Mono.just(userMessage);
            })
            .toFuture();
    }

    // 3. 최종 통합 처리: 욕설 판별 + 순화 처리
    @Async
    public CompletableFuture<String> filterMessageAsync(String userMessage, double threshold) {
        return getToxicityScoreAsync(userMessage).thenCompose(score -> {
            if (score < threshold) {
                logger.info("욕설 아님 (score: {}). 원문 사용", score);
                return CompletableFuture.completedFuture(userMessage);
            } else {
                logger.info("욕설 의심 (score: {}). 순화 요청", score);
                return requestClovaFilteredMessage(userMessage);
            }
        });
    }
}