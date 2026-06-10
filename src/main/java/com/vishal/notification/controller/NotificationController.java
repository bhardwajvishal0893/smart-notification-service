package com.vishal.notification.controller;

import com.vishal.notification.dto.NotificationHistoryResponse;
import com.vishal.notification.dto.SendNotificationRequest;
import com.vishal.notification.dto.SendNotificationResponse;
import com.vishal.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send a notification. AI will decide the best channel automatically.
     *
     * POST /api/notifications/send
     * {
     *   "userId": 1,
     *   "title": "Your order has been placed",
     *   "message": "Order #12345 confirmed. Estimated delivery: 30 mins",
     *   "priority": "HIGH"
     * }
     */
    @PostMapping("/send")
    public ResponseEntity<SendNotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {

        SendNotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Get paginated notification history for a user.
     *
     * GET /api/notifications/{userId}?page=0&size=20
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationHistoryResponse>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(notificationService.getNotificationHistory(userId, page, size));
    }
}
