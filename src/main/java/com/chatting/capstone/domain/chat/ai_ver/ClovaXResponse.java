package com.chatting.capstone.domain.chat.ai_ver;

import lombok.Data;

@Data
public class ClovaXResponse {

    // 전체 응답의 상태 정보를 담는 필드 (성공 여부, 코드, 메시지 등)
    private Status status;

    // AI 응답의 실질적인 결과가 담기는 필드
    private Result result;

    @Data
    public static class Status {
        // 예: "20000" (성공), "40000" (실패)
        private String code;

        // 예: "OK" 또는 "Bad Request" 등 상태 메시지
        private String message;
    }

    @Data
    public static class Result {
        // AI가 생성한 메시지 (본문)
        private Message message;

        // 생성이 끝난 이유 (예: "stop", "length" 등)
        private String finishReason;
    }

    @Data
    public static class Message {
        // 역할 (예: "assistant", "user")
        private String role;

        // 필터링된 텍스트 결과 (실제로 우리가 쓰는 부분)
        private String content;
    }
}
