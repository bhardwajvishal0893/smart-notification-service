package com.vishal.notification.kafka;

import com.vishal.notification.dto.NotificationEvent;
import com.vishal.notification.entity.Notification;
import com.vishal.notification.entity.NotificationStatus;
import com.vishal.notification.repository.NotificationRepository;
import com.vishal.notification.service.ChannelDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final ChannelDispatchService channelDispatchService;

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group",
            concurrency = "3" // 3 consumer threads for parallel processing
    )
    public void consume(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Consuming notification: id={}, channel={}, partition={}, offset={}",
                event.getNotificationId(), event.getChannelDecided(), partition, offset);

        Notification notification = notificationRepository.findById(event.getNotificationId())
                .orElse(null);

        if (notification == null) {
            log.warn("Notification not found in DB: {}", event.getNotificationId());
            return;
        }

        try {
            // Update status to processing
            notification.setStatus(NotificationStatus.RETRYING);
            notificationRepository.save(notification);

            // Dispatch to the decided channel
            channelDispatchService.dispatch(event);

            // Mark as sent
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Notification sent successfully: id={}, channel={}",
                    event.getNotificationId(), event.getChannelDecided());

        } catch (Exception e) {
            log.error("Failed to send notification: id={}, error={}",
                    event.getNotificationId(), e.getMessage());

            notification.setStatus(NotificationStatus.FAILED);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
    }
}
