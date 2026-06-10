package com.vishal.notification.dto;

import com.vishal.notification.entity.NotificationChannel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRoutingDecision {
    private NotificationChannel channel;
    private String reason;
    private double confidenceScore;
}
