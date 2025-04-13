package com.chatting.capstone.domain.room.dto.response;

import com.chatting.capstone.domain.user.dto.response.UserResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String roomName;
    private List<UserResponse> users;
}
