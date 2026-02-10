package com.gbm.taskapi.dto;

import com.gbm.taskapi.model.Role;
import lombok.Builder;

@Builder
public record TokenInfo(Long userId, String email, Role role) {}
