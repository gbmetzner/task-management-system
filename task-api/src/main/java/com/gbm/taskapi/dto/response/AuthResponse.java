package com.gbm.taskapi.dto.response;

import com.gbm.taskapi.model.Role;

public record AuthResponse(
        String token,
        String type, // Always "Bearer"
        Long userId,
        String email,
        String firstName,
        String lastName,
        Role role) {

    public AuthResponse(String token, Long userId, String email, String firstName, String lastName, Role role) {
        this(token, "Bearer", userId, email, firstName, lastName, role);
    }
}
