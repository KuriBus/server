package com.chatting.capstone.global.util;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends TextWebSocketHandler {

    // 방별로 클라이언트 세션을 관리하는 구조
    private Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 클라이언트가 연결될 때 호출 (방 정보는 URL 또는 메시지에서 추출 가능)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomIdFromSession(session); // 방 ID 추출
        roomSessions.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("Client connected to room: " + roomId);
    }

    // 클라이언트가 메시지를 보낼 때 호출
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = getRoomIdFromSession(session); // 현재 세션의 방 ID를 가져옴
        String payload = message.getPayload(); // 클라이언트가 보낸 메시지

        // 같은 방의 모든 클라이언트에게 메시지 브로드캐스팅
        for (WebSocketSession ws : roomSessions.getOrDefault(roomId, Collections.emptySet())) {
            if (ws.isOpen()) {
                ws.sendMessage(new TextMessage(payload));
            }
        }
    }

    // 클라이언트가 연결을 끊었을 때 호출
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomIdFromSession(session);

        // 해당 세션을 방에서 제거
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);

            // 만약 방이 비어 있으면 삭제
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
        System.out.println("Client disconnected from room: " + roomId);
    }

    // 방 ID를 세션에서 추출하는 메서드 (예: URL 또는 초기 메시지에서 가져옴)
    private String getRoomIdFromSession(WebSocketSession session) {
        // 예: URL 쿼리 파라미터에서 방 ID 추출
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf("/") + 1); // URL 끝부분을 방 ID로 사용
    }
}
