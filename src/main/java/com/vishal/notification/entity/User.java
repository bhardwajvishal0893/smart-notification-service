package com.vishal.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "device_token")
    private String deviceToken;

    // User's preferred channels in order of preference
    @ElementCollection
    @CollectionTable(name = "user_channel_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private List<NotificationChannel> channelPreferences;

    @Column(name = "timezone")
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "quiet_hours_start")
    @Builder.Default
    private Integer quietHoursStart = 22; // 10 PM

    @Column(name = "quiet_hours_end")
    @Builder.Default
    private Integer quietHoursEnd = 8; // 8 AM

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
