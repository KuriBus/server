package com.chatting.capstone.domain.chat.entity;

import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 채팅 ID

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;  // 방 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // 유저 ID

    @Column(length = 30)
    private String nickname;  // 유저 닉네임

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;  // 채팅 내용

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;  //채팅 생성 시간
}
