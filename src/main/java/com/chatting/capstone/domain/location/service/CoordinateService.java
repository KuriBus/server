package com.chatting.capstone.domain.location.service;

import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoordinateService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    // Redis Key Prefix (닉네임 기반)
    private static final String USER_LOCATION_KEY_PREFIX = "user:location:";

    // Hash 필드 이름
    private static final String FIELD_ROOM_NAME = "roomName";
    private static final String FIELD_X = "x";
    private static final String FIELD_Y = "y";

    // 사용자의 위치 정보를 Redis Hash에 저장/업데이트
    public void setUserLocation(String nickname, String roomName, int x, int y) {
        boolean exists = userRepository.existsByNickname(nickname);
        if (!exists) {
            throw new CustomException(ResponseStatus.USER_NOT_FOUND);
        }

        String key = USER_LOCATION_KEY_PREFIX + nickname;
        try {
            Map<String, Object> saveValue = new HashMap<>();
            saveValue.put(FIELD_ROOM_NAME, roomName);
            saveValue.put(FIELD_X, String.valueOf(x));
            saveValue.put(FIELD_Y, String.valueOf(y));
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            hashOps.putAll(key, saveValue);
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            throw new CustomException(ResponseStatus.SERVER_ERROR);
        }
    }

    // 특정 사용자의 전체 위치 정보(Map)를 Redis에서 가져옴
    public Map<String, Object> getUserLocation(String nickname) {
        String key = USER_LOCATION_KEY_PREFIX + nickname;
        try {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            Map<String, Object> result = hashOps.entries(key);
            if (result == null) {
                throw new CustomException(ResponseStatus.COORDINATE_NOT_FOUND);
            }
            return result;
        } catch (Exception e) {
            throw new CustomException(ResponseStatus.SERVER_ERROR);
        }
    }

    // 특정 사용자의 현재 방 이름만 가져옴
    public Optional<String> getCurrentRoomName(String nickname) {
        String key = USER_LOCATION_KEY_PREFIX + nickname;
        try {
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
            return Optional.ofNullable(hashOps.get(key, FIELD_ROOM_NAME));
        } catch (Exception e) {
            throw new CustomException(ResponseStatus.SERVER_ERROR);
        }
    }

    // 특정 사용자의 위치 정보를 Redis에서 삭제 (로그아웃, 연결 종료 시)
    public void deleteUserLocation(String nickname) {
        String key = USER_LOCATION_KEY_PREFIX + nickname;
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new CustomException(ResponseStatus.SERVER_ERROR);
        }
    }
}
