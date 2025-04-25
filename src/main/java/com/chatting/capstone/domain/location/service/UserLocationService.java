package com.chatting.capstone.domain.location.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserLocationService {
    private final RedisTemplate<String, String> redisTemplate;

    // Redis Key Prefix (사용자 ID 기반)
    private static final String USER_LOCATION_KEY_PREFIX = "user:location:";

    // Hash 필드 이름
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_X = "x";
    private static final String FIELD_Y = "y";

    // 사용자의 위치 정보를 Redis Hash에 저장/업데이트
    public void setUserLocation(String userId, String roomName, int x, int y) {
        String key = USER_LOCATION_KEY_PREFIX + userId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put(key, FIELD_ROOM_NAME, roomName);
        hashOps.put(key, FIELD_X, String.valueOf(x));
        hashOps.put(key, FIELD_Y, String.valueOf(y));
         redisTemplate.expire(key, 1, TimeUnit.HOURS); // 1시간 후 자동 삭제
    }

    // 특정 사용자의 전체 위치 정보(Map)를 Redis에서 가져옴
    public Map<String, String> getUserLocation(String userId) {
        String key = USER_LOCATION_KEY_PREFIX + userId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return hashOps.entries(key);
    }

    // 특정 사용자의 현재 방 이름만 가져옴
    public Optional<String> getCurrentRoomName(String userId) {
        String key = USER_LOCATION_KEY_PREFIX + userId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return Optional.ofNullable(hashOps.get(key, FIELD_ROOM_NAME));
    }

    // 특정 사용자의 위치 정보를 Redis에서 삭제 (로그아웃, 연결 종료 시)
    public void deleteUserLocation(String userId) {
        String key = USER_LOCATION_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
