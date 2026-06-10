package com.vishal.notification.dto;

import com.vishal.notification.entity.NotificationChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "email is required")
    @Email(message = "invalid email format")
    private String email;

    @NotBlank(message = "name is required")
    private String name;

    private String phoneNumber;
    private String deviceToken;

    @Builder.Default
    private List<NotificationChannel> channelPreferences = List.of(
            NotificationChannel.PUSH,
            NotificationChannel.EMAIL,
            NotificationChannel.SMS
    );

    private Integer quietHoursStart;
    private Integer quietHoursEnd;
}
