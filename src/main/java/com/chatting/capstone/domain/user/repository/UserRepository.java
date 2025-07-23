package com.chatting.capstone.domain.user.repository;

import com.chatting.capstone.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    boolean existsByNickname(String nickname);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
