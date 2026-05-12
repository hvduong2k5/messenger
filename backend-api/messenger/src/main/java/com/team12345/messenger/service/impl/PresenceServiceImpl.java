package com.team12345.messenger.service.impl;

import com.team12345.messenger.entity.User;
import com.team12345.messenger.gateway.MqttGateway;
import com.team12345.messenger.repository.UserFriendRepository;
import com.team12345.messenger.repository.UserRepository;
import com.team12345.messenger.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceServiceImpl implements PresenceService {

    private final UserRepository userRepository;
    private final UserFriendRepository userFriendRepository;
    private final MqttGateway mqttGateway;

    @Override
    @Transactional
    public void updateUserPresence(String username, boolean isOnline) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            log.warn("[Presence] Unknown user: {}", username);
            return;
        }

        user.setIsOnline(isOnline);
        if (!isOnline) {
            user.setLastSeen(LocalDateTime.now());
        }
        userRepository.save(user);

        // Broadcast to all friends
        String payload = String.format("{\"userId\":%d,\"status\":\"%s\"}", user.getId(), isOnline ? "online" : "offline");
        userFriendRepository.findById_UserId(user.getId()).forEach(uf -> {
            String topic = "user/" + uf.getFriend().getId() + "/presence";
            try {
                mqttGateway.sendToMqtt(payload, topic);
            } catch (Exception e) {
                log.error("[Presence] Failed to notify friend {} : {}", uf.getFriend().getId(), e.getMessage());
            }
        });

        log.info("[Presence] User {} is now {}", username, isOnline ? "online" : "offline");
    }
}
