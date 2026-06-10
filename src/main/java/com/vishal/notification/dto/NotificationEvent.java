package com.vishal.notification.dto;

import com.vishal.notification.entity.NotificationChannel;
import com.vishal.notification.entity.NotificationPriority;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private Long notificationId;
    private Long userId;
    private String title;
    private String message;
    private NotificationPriority priority;
    private NotificationChannel channelDecided;
    private String aiRoutingReason;
    private LocalDateTime createdAt;
}
