package com.gbm.taskapi.dto.response;

import com.gbm.taskapi.model.Role;

public record AuthResponse(
        String accessToken,
        String type, // Always "Bearer"
        String refreshToken,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Role role) {

    public AuthResponse(
            String accessToken,
            String refreshToken,
            Long userId,
            String email,
            String firstName,
            String lastName,
            Role role) {
        this(accessToken, "Bearer", refreshToken, userId, email, firstName, lastName, role);
    }
}
