package com.chatting.capstone.domain.customization.repository;

import com.chatting.capstone.domain.customization.entity.Customization;
import com.chatting.capstone.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomizationRepository extends JpaRepository<Customization, Long> {
    Optional<Customization> findByUser(User user);
}
