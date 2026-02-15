package com.gbm.taskapi.dto.response;

import com.gbm.taskapi.model.User;

public record UserSummary(Long id, String email, String firstName, String lastName, String fullName) {
    public UserSummary(User user) {
        this(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFirstName() + " " + user.getLastName());
    }
}
