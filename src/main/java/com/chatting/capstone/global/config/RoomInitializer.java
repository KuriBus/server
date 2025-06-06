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
        createRoomIfNotExists("교실"); // room_id: 1
        createRoomIfNotExists("공원"); // room_id: 2
        createRoomIfNotExists("문화공간"); // room_id: 3

        // 통로 방
        createRoomIfNotExists("통로 1"); // room_id: 4
        createRoomIfNotExists("통로 2"); // room_id: 5
        createRoomIfNotExists("통로 3"); // room_id: 6
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
