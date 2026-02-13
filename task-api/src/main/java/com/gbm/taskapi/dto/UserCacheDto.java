package com.gbm.taskapi.dto;

import com.gbm.taskapi.model.Role;

public record UserCacheDto(Long id, String email, String firstName, String lastName, Role role) {}
