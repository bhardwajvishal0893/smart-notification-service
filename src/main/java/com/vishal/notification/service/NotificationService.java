package com.vishal.notification.service;

import com.vishal.notification.ai.AiRoutingService;
import com.vishal.notification.dto.*;
import com.vishal.notification.entity.Notification;
import com.vishal.notification.entity.NotificationStatus;
import com.vishal.notification.entity.User;
import com.vishal.notification.kafka.NotificationProducer;
import com.vishal.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final AiRoutingService aiRoutingService;
    private final NotificationProducer notificationProducer;

    @Transactional
    public SendNotificationResponse sendNotification(SendNotificationRequest request) {
        // 1. Fetch user (cached via Redis)
        User user = userService.getUserById(request.getUserId());

        // 2. AI decides the best channel
        log.info("Requesting AI routing decision for userId={}, priority={}",
                user.getId(), request.getPriority());

        AiRoutingDecision routingDecision = aiRoutingService.decideChannel(
                user,
                request.getTitle(),
                request.getMessage(),
                request.getPriority()
        );

        log.info("AI routing decision: channel={}, confidence={}, reason={}",
                routingDecision.getChannel(),
                routingDecision.getConfidenceScore(),
                routingDecision.getReason());

        // 3. Persist notification record
        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .priority(request.getPriority())
                .channelUsed(routingDecision.getChannel())
                .status(NotificationStatus.QUEUED)
                .aiRoutingReason(routingDecision.getReason())
                .build();

        Notification saved = notificationRepository.save(notification);

        // 4. Publish to Kafka for async processing
        NotificationEvent event = NotificationEvent.builder()
                .notificationId(saved.getId())
                .userId(user.getId())
                .title(request.getTitle())
                .message(request.getMessage())
                .priority(request.getPriority())
                .channelDecided(routingDecision.getChannel())
                .aiRoutingReason(routingDecision.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        notificationProducer.publishNotification(event);

        return SendNotificationResponse.builder()
                .notificationId(saved.getId())
                .status("QUEUED")
                .channelUsed(routingDecision.getChannel())
                .aiRoutingReason(routingDecision.getReason())
                .queuedAt(LocalDateTime.now())
                .build();
    }

    public List<NotificationHistoryResponse> getNotificationHistory(Long userId, int page, int size) {
        // Validate user exists
        userService.getUserById(userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return notifications.getContent().stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private NotificationHistoryResponse toHistoryResponse(Notification n) {
        return NotificationHistoryResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .priority(n.getPriority())
                .channelUsed(n.getChannelUsed())
                .status(n.getStatus())
                .aiRoutingReason(n.getAiRoutingReason())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .build();
    }
}
