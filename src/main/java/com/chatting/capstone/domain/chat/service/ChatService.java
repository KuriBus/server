package com.chatting.capstone.domain.chat.service;

import com.chatting.capstone.domain.chat.ai_ver.ClovaXClient;
import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.entity.Chat;
import com.chatting.capstone.domain.chat.redis.RedisPublisher;
import com.chatting.capstone.domain.chat.repository.ChatRepository;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.moderation.AiModerationResponse;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final ClovaXClient clovaXClient;

    private static final int TIME_WINDOW_MILLIS = 5000;  // 5초
    private static final int MAX_MESSAGES = 5; //5초 안에 5번 채팅
    private static final int MAX_REPEAT = 3; //동일 내용 3회 반복
    private static final int MUTE_DURATION_MILLIS = 30 * 1000;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisPublisher redisPublisher;

    private final Map<String, Long> muteMap = new ConcurrentHashMap<>();

    public ChatResponse save(ChatRequest dto, String filteredContent, double maliceScore) {
        validateContent(dto.getContent(), dto.getNickname());

        Room room = roomRepository.findById(dto.getRoomId())
            .orElseThrow(() -> new CustomException(ResponseStatus.ROOM_NOT_FOUND));

        User user = userRepository.findByNickname(dto.getNickname())
            .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        Chat chat = Chat.builder()
            .room(room)
            .user(user)
            .nickname(dto.getNickname())
            .originalContent(dto.getContent())
            .filteredContent(filteredContent)
            .maliceScore(maliceScore)
            .createdAt(LocalDateTime.now())
            .build();

        chatRepository.save(chat);

        ChatResponse response = ChatResponse.builder()
            .id(chat.getId())
            .userId(chat.getUser().getId())
            .nickname(chat.getNickname())
            .content(chat.getFilteredContent())
            .maliceScore(chat.getMaliceScore())
            .createdAt(chat.getCreatedAt())
            .build();

        saveChatMessageToRedis(dto.getRoomId(), response);

        return response;
    }

    public List<ChatResponse> getChatsByRoom(Long roomId) {
        return chatRepository.findAllByRoomIdOrderByCreatedAtAsc(roomId).stream()
            .map(chat -> ChatResponse.builder()
                .id(chat.getId())
                .userId(chat.getUser().getId())
                .nickname(chat.getNickname())
                .content(chat.getFilteredContent())
                .createdAt(chat.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    //몇초 남았는지 계산
    public long getMuteRemainingMillis(String nickname) {
        String key = "mute:" + nickname;
        Long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        return (expire == null || expire < 0) ? 0 : expire;
    }

    public boolean isMuted(String nickname) {
        String key = "mute:" + nickname;
        Boolean hasKey = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    public void mute(String nickname) {
        muteMap.put(nickname, System.currentTimeMillis() + MUTE_DURATION_MILLIS);

        String key = "mute:" + nickname;
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(30));
    }

    // 도배 감지 로직
    public boolean isSpamming(String nickname, String content) {
        List<ChatRecord> messages = getRecentSpamRecords(nickname);
        long now = System.currentTimeMillis();

        // 새로운 메시지 기록 추가
        messages.add(new ChatRecord(content, now));
        addSpamRecord(nickname, content);  // Redis에 저장

        if (messages.size() >= MAX_MESSAGES) return true;

        long repeatCount = messages.stream()
            .filter(m -> m.content.equals(content))
            .count();

        return repeatCount >= MAX_REPEAT;
    }

    // 메시지 저장용 내부 클래스
    private static class ChatRecord {
        private String content;
        private long timestamp;

        @JsonCreator
        public ChatRecord(@JsonProperty("content") String content,
            @JsonProperty("timestamp") long timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getContent() {
            return content;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public void validateContent(String content, String nickname) {
        int MAX_LENGTH = 30;
        if (content == null || content.trim().isEmpty()) {
            // 사용자에게 WebSocket으로 오류 전송
            publishErrorToUser(nickname, "빈 메시지는 전송할 수 없습니다.");
            // 서버 로깅 + 흐름 차단을 위해 예외 던짐
            throw new CustomException(ResponseStatus.INVALID_MESSAGE);
        }

        if (content.length() > MAX_LENGTH) {
            publishErrorToUser(nickname, "메시지가 너무 깁니다. 30자 이하로 입력해주세요.");
            throw new CustomException(ResponseStatus.MESSAGE_TOO_LONG);
        }
    }

    public void processMessage(ChatRequest dto, String nickname, long startTime) {

        if (isMuted(nickname)) {
            long remaining = Math.max(1, getMuteRemainingMillis(nickname) / 1000);

            publishWarningToUser(nickname, "⛔ 현재 도배로 인해 채팅이 제한되었습니다. 남은 시간: " + remaining + "초");

            throw new CustomException(ResponseStatus.MUTED);
        }

        if (isSpamming(nickname, dto.getContent())) {
            mute(nickname);
            publishWarningToUser(nickname, "⚠️ 도배로 판단되어 채팅이 30초간 제한됩니다.");
            throw new CustomException(ResponseStatus.SPAM_DETECTED);
        }

        validateContent(dto.getContent(), nickname); // 비속어, 글자수 검증

        String originalContent = dto.getContent();
        String purifiedText = originalContent;

        double maliceScore = 0.0;

        try {
            // ClovaXClient 호출
            AiModerationResponse moderation = clovaXClient.filterMessage(originalContent);

            if (moderation != null && moderation.getPurified_text() != null && !moderation.getPurified_text().isEmpty()) {
                purifiedText = moderation.getPurified_text();
            }
            maliceScore = moderation.getMalice_score();
        } catch (Exception e) {
            log.error("ClovaXClient 호출 실패, 원본 텍스트 사용", e);
        }

        // DB 저장
        ChatResponse chatResponse = save(dto, purifiedText, maliceScore);// save() 안에서 filteredContent = purifiedText

        // Redis 발행 (항상 filteredContent 사용)
        try {
            String topic = "chat:room:" + dto.getRoomId();
            redisPublisher.publish(topic, chatResponse); // ChatResponse.content는 항상 filteredContent
            saveChatMessageToRedis(dto.getRoomId(), chatResponse);
        } catch (Exception e) {
            log.error("Redis 메시지 발행 오류", e);
        }
    }
    // 사용자에게 에러 메시지 Redis로 전송
    public void publishErrorToUser(String nickname, String errorMessage) {
        redisPublisher.publish("errors:" + nickname, errorMessage);
    }

    public void publishWarningToUser(String nickname, String warningMessage) {
        redisPublisher.publish("warnings:" + nickname, warningMessage);
    }
    // 1) 채팅 메시지 Redis 저장 (최대 50개 유지)
    private void saveChatMessageToRedis(Long roomId, ChatResponse chatResponse) {
        String key = "chat:room:" + roomId;
        try {
            String json = objectMapper.writeValueAsString(chatResponse);
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.opsForList().trim(key, -50, -1);
        } catch (Exception e) {
            log.error("Redis 채팅 메시지 저장 오류", e);
        }
    }

    // 2) 도배 기록 Redis 저장
    private void addSpamRecord(String nickname, String content) {
        String key = "spam:history:" + nickname;
        long now = System.currentTimeMillis();
        try {
            String json = objectMapper.writeValueAsString(new ChatRecord(content, now));
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.expire(key, Duration.ofMinutes(1));  // TTL 설정
        } catch (Exception e) {
            log.error("Redis 도배 기록 저장 오류", e);
        }
    }

    // 3) 도배 기록 Redis 조회 (최근 5초 이내 메시지만 필터링)
    private List<ChatRecord> getRecentSpamRecords(String nickname) {
        String key = "spam:history:" + nickname;
        try {
            List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
            if (jsonList == null) return new ArrayList<>();
            List<ChatRecord> records = new ArrayList<>();
            for (String json : jsonList) {
                ChatRecord record = objectMapper.readValue(json, ChatRecord.class);
                records.add(record);
            }
            long now = System.currentTimeMillis();
            records.removeIf(r -> now - r.timestamp > TIME_WINDOW_MILLIS);
            return records;
        } catch (Exception e) {
            log.error("Redis 도배 기록 조회 오류", e);
            return new ArrayList<>();
        }
    }
}
