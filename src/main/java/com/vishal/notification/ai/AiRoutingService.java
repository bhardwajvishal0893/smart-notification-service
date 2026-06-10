package com.vishal.notification.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishal.notification.dto.AiRoutingDecision;
import com.vishal.notification.entity.NotificationChannel;
import com.vishal.notification.entity.NotificationPriority;
import com.vishal.notification.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRoutingService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * Uses OpenAI to decide the best notification channel based on:
     * - Message priority
     * - User's channel preferences
     * - Current time vs user's quiet hours
     * - Available channels (phone, email, device token)
     */
    public AiRoutingDecision decideChannel(User user, String title, String message,
                                           NotificationPriority priority) {
        try {
            String prompt = buildRoutingPrompt(user, title, message, priority);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseDecision(response, user);

        } catch (Exception e) {
            log.error("AI routing failed, falling back to default channel. Error: {}", e.getMessage());
            return fallbackDecision(user, priority);
        }
    }

    private String buildRoutingPrompt(User user, String title, String message,
                                      NotificationPriority priority) {
        LocalTime currentTime = LocalTime.now(ZoneId.of(user.getTimezone()));
        boolean isQuietHours = isWithinQuietHours(currentTime, user.getQuietHoursStart(), user.getQuietHoursEnd());

        List<String> availableChannels = getAvailableChannels(user);
        List<String> userPreferences = user.getChannelPreferences() != null
                ? user.getChannelPreferences().stream().map(Enum::name).toList()
                : List.of("EMAIL");

        return String.format("""
                You are a smart notification routing engine. Decide the best channel to send a notification.
                
                NOTIFICATION DETAILS:
                - Title: %s
                - Message: %s
                - Priority: %s
                
                USER CONTEXT:
                - Available channels: %s
                - User preferred channels (in order): %s
                - Current time: %s
                - User's quiet hours: %d:00 to %d:00
                - Is currently quiet hours: %s
                
                ROUTING RULES:
                1. CRITICAL priority: Always use SMS if available, regardless of quiet hours
                2. HIGH priority: Use PUSH if available, fall back to EMAIL. Respect quiet hours only if LOW/MEDIUM
                3. MEDIUM priority: Follow user preferences, skip quiet hours channels
                4. LOW priority: Use EMAIL always, never disturb with PUSH/SMS during quiet hours
                5. If no preferred channel is available, use any available channel
                
                Respond ONLY in this exact JSON format, no explanation outside JSON:
                {
                  "channel": "EMAIL|SMS|PUSH",
                  "reason": "brief one-line reason",
                  "confidenceScore": 0.0-1.0
                }
                """,
                title, message, priority.name(),
                availableChannels,
                userPreferences,
                currentTime,
                user.getQuietHoursStart(), user.getQuietHoursEnd(),
                isQuietHours
        );
    }

    private AiRoutingDecision parseDecision(String response, User user) {
        try {
            // Clean response in case AI adds markdown fences
            String cleaned = response.trim()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            AiRoutingDecision decision = objectMapper.readValue(cleaned, AiRoutingDecision.class);

            // Validate channel is actually available for this user
            if (!isChannelAvailable(decision.getChannel(), user)) {
                log.warn("AI chose unavailable channel {}, falling back", decision.getChannel());
                return fallbackDecision(user, null);
            }

            return decision;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", response, e);
            return fallbackDecision(user, null);
        }
    }

    private AiRoutingDecision fallbackDecision(User user, NotificationPriority priority) {
        // Rule-based fallback if AI fails
        NotificationChannel channel;

        if (priority == NotificationPriority.CRITICAL && user.getPhoneNumber() != null) {
            channel = NotificationChannel.SMS;
        } else if (user.getDeviceToken() != null) {
            channel = NotificationChannel.PUSH;
        } else {
            channel = NotificationChannel.EMAIL;
        }

        return AiRoutingDecision.builder()
                .channel(channel)
                .reason("Fallback rule-based routing (AI unavailable)")
                .confidenceScore(0.6)
                .build();
    }

    private List<String> getAvailableChannels(User user) {
        List<String> channels = new java.util.ArrayList<>();
        channels.add("EMAIL"); // email always available
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            channels.add("SMS");
        }
        if (user.getDeviceToken() != null && !user.getDeviceToken().isBlank()) {
            channels.add("PUSH");
        }
        return channels;
    }

    private boolean isChannelAvailable(NotificationChannel channel, User user) {
        return switch (channel) {
            case EMAIL -> true;
            case SMS -> user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank();
            case PUSH -> user.getDeviceToken() != null && !user.getDeviceToken().isBlank();
        };
    }

    private boolean isWithinQuietHours(LocalTime current, int start, int end) {
        int hour = current.getHour();
        if (start > end) {
            // Spans midnight e.g. 22:00 to 08:00
            return hour >= start || hour < end;
        }
        return hour >= start && hour < end;
    }
}
