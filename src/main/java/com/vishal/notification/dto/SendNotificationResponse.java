package com.vishal.notification.dto;

import com.vishal.notification.entity.NotificationChannel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationResponse {
    private Long notificationId;
    private String status;
    private NotificationChannel channelUsed;
    private String aiRoutingReason;
    private LocalDateTime queuedAt;
}
