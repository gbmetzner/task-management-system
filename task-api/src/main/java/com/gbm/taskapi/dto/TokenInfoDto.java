package com.gbm.taskapi.dto;

import com.gbm.taskapi.model.Role;
import lombok.Builder;

@Builder
public record TokenInfoDto(Long userId, String email, Role role) {}
