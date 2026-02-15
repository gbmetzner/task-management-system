package com.gbm.taskapi.dto.service;

import com.gbm.taskapi.model.Role;

public record AuthResult(String token, Long userId, String email, String firstName, String lastName, Role role) {}
