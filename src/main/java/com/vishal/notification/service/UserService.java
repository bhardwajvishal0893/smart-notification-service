package com.vishal.notification.service;

import com.vishal.notification.dto.CreateUserRequest;
import com.vishal.notification.entity.User;
import com.vishal.notification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .deviceToken(request.getDeviceToken())
                .channelPreferences(request.getChannelPreferences())
                .quietHoursStart(request.getQuietHoursStart() != null ? request.getQuietHoursStart() : 22)
                .quietHoursEnd(request.getQuietHoursEnd() != null ? request.getQuietHoursEnd() : 8)
                .build();

        User saved = userRepository.save(user);
        log.info("Created user: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @CacheEvict(value = "users", key = "#userId")
    public void evictUserCache(Long userId) {
        log.debug("Evicted cache for user: {}", userId);
    }
}
