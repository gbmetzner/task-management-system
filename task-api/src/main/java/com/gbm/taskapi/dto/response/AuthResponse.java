package com.gbm.taskapi.dto.response;

public record AuthResponse(
        String token,
        String type, // Always "Bearer"
        Long userId,
        String email,
        String firstName,
        String lastName) {

    public AuthResponse(String token, Long userId, String email, String firstName, String lastName) {
        this(token, "Bearer", userId, email, firstName, lastName);
    }
}
