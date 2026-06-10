package com.vishal.notification.kafka;

import com.vishal.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void publishNotification(NotificationEvent event) {
        String key = String.valueOf(event.getUserId()); // partition by userId for ordering

        CompletableFuture<SendResult<String, NotificationEvent>> future =
                kafkaTemplate.send(TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish notification event for notificationId={}: {}",
                        event.getNotificationId(), ex.getMessage());
            } else {
                log.info("Published notification event: notificationId={}, partition={}, offset={}",
                        event.getNotificationId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
