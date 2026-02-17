package com.gbm.taskapi.dto.service;

import com.gbm.taskapi.model.Role;

public record AuthResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Role role) {}
