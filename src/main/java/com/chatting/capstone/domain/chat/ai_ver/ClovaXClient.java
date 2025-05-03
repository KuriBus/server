package com.chatting.capstone.domain.chat.ai_ver;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class ClovaXClient {

    private final WebClient webClient;
    private final String apiKey;

    public ClovaXClient(@Value("${api.clova.ai_key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
            .baseUrl("https://clovastudio.stream.ntruss.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE) // 💡 JSON으로 응답 받기
            .build();
    }

    @Async
    public CompletableFuture<String> filterMessageAsync(String userMessage) {
        log.info("ClovaXClient: 메시지 필터링 요청 시작: '{}'", userMessage);

        if (userMessage == null || userMessage.isEmpty()) {
            log.warn("ClovaXClient: 필터링할 메시지가 비어있거나 null입니다.");
            return CompletableFuture.completedFuture(userMessage);
        }

        long startTime = System.currentTimeMillis();

        String endpoint = String.format("/testapp/v3/chat-completions/HCX-005");

        String prompt = "다음 문장에서 욕을 보기 좋게 수정시켜주세요. 최대한 문장의 의미가 다르지 않게 수정해주세요."
            + "무조건 수정한 문장만 던져줘 다른 부가적인 말과 기호없이 따옴표 같은 것도 다 빼주세요. 원문: " + userMessage;

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

        log.debug("Sending request to ClovaX API: {}", requestBody);

        return webClient.post()
            .uri(endpoint)
            .header("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString())
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(ClovaXResponse.class)
            .doOnNext(response -> log.info("ClovaXClient: API 응답 수신: {}", response))
            .map(response -> {
                if (response == null || response.getResult() == null || response.getResult().getMessage() == null) {
                    log.warn("ClovaXClient: 예상치 못한 응답 구조. 응답: {}", response);
                    return "<<예상치 못한 응답 형식>>";
                }
                String filteredMessage = response.getResult().getMessage().getContent();
                long duration = System.currentTimeMillis() - startTime;
                log.info("ClovaXClient: 필터링 완료 ({} ms). 원본: '{}' → 필터링된 메시지: '{}'", duration, userMessage, filteredMessage);
                return filteredMessage;
            })
            .onErrorResume(ex -> {
                long duration = System.currentTimeMillis() - startTime;
                log.error("ClovaXClient: API 호출 중 오류 발생: {} | 원본 메시지: '{}' | 소요 시간: {} ms", ex.getMessage(), userMessage, duration, ex);
                return Mono.just("<<필터링 실패>>");
            })
            .toFuture();
    }
}

//로그처리하기 시간 계속 확인해보기 test많이 해보기