package com.vishal.notification.service;

import com.vishal.notification.dto.NotificationEvent;
import com.vishal.notification.entity.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * In a real system, each channel would integrate with:
 * - EMAIL: SendGrid / AWS SES
 * - SMS: Twilio / AWS SNS
 * - PUSH: Firebase FCM / Apple APNs
 *
 * For this project, we simulate the dispatch and log it.
 * The architecture is what matters — swapping in real providers is trivial.
 */
@Service
@Slf4j
public class ChannelDispatchService {

    public void dispatch(NotificationEvent event) {
        NotificationChannel channel = event.getChannelDecided();

        switch (channel) {
            case EMAIL -> dispatchEmail(event);
            case SMS -> dispatchSms(event);
            case PUSH -> dispatchPush(event);
            default -> throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    private void dispatchEmail(NotificationEvent event) {
        // TODO: Replace with SendGrid / AWS SES integration
        log.info("[EMAIL DISPATCH] userId={} | title='{}' | reason='{}'",
                event.getUserId(), event.getTitle(), event.getAiRoutingReason());

        // Simulate network latency
        simulateDispatch(100);
    }

    private void dispatchSms(NotificationEvent event) {
        // TODO: Replace with Twilio integration
        log.info("[SMS DISPATCH] userId={} | title='{}' | reason='{}'",
                event.getUserId(), event.getTitle(), event.getAiRoutingReason());

        simulateDispatch(150);
    }

    private void dispatchPush(NotificationEvent event) {
        // TODO: Replace with Firebase FCM integration
        log.info("[PUSH DISPATCH] userId={} | title='{}' | reason='{}'",
                event.getUserId(), event.getTitle(), event.getAiRoutingReason());

        simulateDispatch(50);
    }

    private void simulateDispatch(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
