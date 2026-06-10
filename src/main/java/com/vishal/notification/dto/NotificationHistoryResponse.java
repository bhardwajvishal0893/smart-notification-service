package com.vishal.notification.dto;

import com.vishal.notification.entity.NotificationChannel;
import com.vishal.notification.entity.NotificationPriority;
import com.vishal.notification.entity.NotificationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationHistoryResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationPriority priority;
    private NotificationChannel channelUsed;
    private NotificationStatus status;
    private String aiRoutingReason;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
