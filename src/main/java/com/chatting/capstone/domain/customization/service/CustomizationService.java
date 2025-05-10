package com.chatting.capstone.domain.customization.service;

import com.chatting.capstone.domain.customization.dto.request.CustomizationRequest;
import com.chatting.capstone.domain.customization.dto.response.CustomizationResponse;
import com.chatting.capstone.domain.customization.entity.Customization;
import com.chatting.capstone.domain.customization.repository.CustomizationRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;



@Service
@Slf4j
@RequiredArgsConstructor
public class CustomizationService {

    private final CustomizationRepository customizationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;


    public void updateCustomization(CustomizationRequest customizationRequest) {
        Optional<User> userOptional = userRepository.findByNickname(customizationRequest.getNickname());
        if (userOptional.isEmpty()) throw new RuntimeException("사용자 없음");

        User user = userOptional.get();

        Customization customization = customizationRepository.findByUser(user)
            .orElse(new Customization());

        customization.setUser(user);
        customization.setHairType(customizationRequest.getHairType());
        customization.setOutfitType(customizationRequest.getOutfitType());

        customizationRepository.save(customization);

        // WebSocket 브로드캐스트
        CustomizationResponse response = new CustomizationResponse(
            user.getId(),
            customization.getHairType(),
            customization.getOutfitType()
        );
        System.out.println("Broadcasting customization update: " + response);
        messagingTemplate.convertAndSend("/topic/customization", response);
    }

}
