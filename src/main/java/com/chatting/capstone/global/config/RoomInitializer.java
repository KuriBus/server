package com.chatting.capstone.global.config;

import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomInitializer implements CommandLineRunner {
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 메인 방
        createRoomIfNotExists("교실");
        createRoomIfNotExists("공원");
        createRoomIfNotExists("문화공간");

        // 통로 방
        createRoomIfNotExists("통로 1");
        createRoomIfNotExists("통로 2");
        createRoomIfNotExists("통로 3");
    }

    private void createRoomIfNotExists(String roomName) {
        if (!roomRepository.existsByRoomName(roomName)) {
            roomRepository.save(
                    Room.builder()
                            .roomName(roomName)
                            .build()
            );
        }
    }
}
