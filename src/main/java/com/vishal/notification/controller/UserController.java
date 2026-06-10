package com.vishal.notification.controller;

import com.vishal.notification.dto.CreateUserRequest;
import com.vishal.notification.entity.User;
import com.vishal.notification.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Create a user with their notification preferences.
     *
     * POST /api/users
     * {
     *   "email": "vishal@example.com",
     *   "name": "Vishal",
     *   "phoneNumber": "+919876543210",
     *   "deviceToken": "firebase-token-xyz",
     *   "channelPreferences": ["PUSH", "EMAIL", "SMS"],
     *   "quietHoursStart": 22,
     *   "quietHoursEnd": 8
     * }
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
}
