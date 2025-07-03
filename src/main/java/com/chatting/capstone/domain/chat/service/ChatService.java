package com.chatting.capstone.domain.chat.service;

import com.chatting.capstone.domain.chat.ai_ver.ClovaXClient;
import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.entity.Chat;
import com.chatting.capstone.domain.chat.repository.ChatRepository;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.moderation.ClovaService;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final ClovaService clovaService;
    private final ClovaXClient clovaXClient;

    private static final int TIME_WINDOW_MILLIS = 5000;  // 5초
    private static final int MAX_MESSAGES = 5; //5초 안에 5번 채팅
    private static final int MAX_REPEAT = 3; //동일 내용 3회 반복
    private static final int MUTE_DURATION_MILLIS = 30 * 1000;

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, List<ChatRecord>> messageHistory = new ConcurrentHashMap<>();
    private final Map<String, Long> muteMap = new ConcurrentHashMap<>();

    public ChatResponse save(ChatRequest dto, String filteredContent) {
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
            .createdAt(LocalDateTime.now())
            .build();

        chatRepository.save(chat);

        return ChatResponse.builder()
            .id(chat.getId())
            .userId(chat.getUser().getId())
            .nickname(chat.getNickname())
            .content(chat.getFilteredContent())
            .createdAt(chat.getCreatedAt())
            .build();
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
        Long until = muteMap.get(nickname);
        if (until == null) return 0;

        long now = System.currentTimeMillis();
        return Math.max(0, until - now);
    }

    public boolean isMuted(String nickname) {
        return getMuteRemainingMillis(nickname) > 0;
    }

    public void mute(String nickname) {
        muteMap.put(nickname, System.currentTimeMillis() + MUTE_DURATION_MILLIS);
    }

    // 도배 감지 로직
    public boolean isSpamming(String nickname, String content) {
        long now = System.currentTimeMillis();

        messageHistory.putIfAbsent(nickname, new ArrayList<>());
        List<ChatRecord> messages = messageHistory.get(nickname);

        messages.removeIf(record -> now - record.timestamp > TIME_WINDOW_MILLIS);
        messages.add(new ChatRecord(content, now));

        if (messages.size() >= MAX_MESSAGES) return true;

        long repeatCount = messages.stream()
            .filter(m -> m.content.equals(content))
            .count();

        return repeatCount >= MAX_REPEAT;
    }

    // 메시지 저장용 내부 클래스
    private static class ChatRecord {
        String content;
        long timestamp;

        ChatRecord(String content, long timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    public void validateContent(String content, String nickname) {
        int MAX_LENGTH = 30;
        if (content == null || content.trim().isEmpty()) {
            // 사용자에게 WebSocket으로 오류 전송
            messagingTemplate.convertAndSend("/queue/errors/" + nickname, "빈 메시지는 전송할 수 없습니다.");
            // 서버 로깅 + 흐름 차단을 위해 예외 던짐
            throw new CustomException(ResponseStatus.INVALID_MESSAGE);
        }

        if (content.length() > MAX_LENGTH) {
            messagingTemplate.convertAndSend("/queue/errors/" + nickname, "메시지가 너무 깁니다. 30자 이하로 입력해주세요.");
            throw new CustomException(ResponseStatus.MESSAGE_TOO_LONG);
        }
    }

    public Mono<ChatResponse> processMessage(ChatRequest dto, String nickname) {

        if (isMuted(nickname)) {
            long remainingMillis = getMuteRemainingMillis(nickname);
            long secondsLeft = Math.max(1, remainingMillis / 1000); // 최소 1초 보장

            messagingTemplate.convertAndSend("/queue/warnings/" + nickname,
                "⛔ 현재 도배로 인해 채팅이 제한되었습니다. 남은 시간: " + secondsLeft + "초");

            throw new CustomException(ResponseStatus.MUTED);
        }

        if (isSpamming(nickname, dto.getContent())) {
            mute(nickname);
            messagingTemplate.convertAndSend("/queue/warnings/" + nickname,
                "⚠️ 도배로 판단되어 채팅이 30초간 제한됩니다.");
            throw new CustomException(ResponseStatus.SPAM_DETECTED);
        }

        validateContent(dto.getContent(), nickname); // 비속어, 글자수 검증

        String originalContent = dto.getContent();
        long appraisalStart = System.currentTimeMillis();

        return clovaService.appraiseSentence(originalContent)
            .flatMap(isInappropriate -> {
                long appraisalEnd = System.currentTimeMillis();
                log.info("[{}] Clova appraisal: {}ms", nickname, appraisalEnd - appraisalStart);

                if ("1".equals(isInappropriate)) {
                    messagingTemplate.convertAndSend("/queue/warnings/" + nickname,
                        "⚠️ 부적절한 표현이 감지되어 자동으로 수정되었습니다.");
                    return clovaXClient.filterMessageAsync(originalContent)
                        .map(filteredContent -> save(dto, filteredContent));
                } else {
                    return Mono.just(save(dto, originalContent));
                }
            });
    }
}
