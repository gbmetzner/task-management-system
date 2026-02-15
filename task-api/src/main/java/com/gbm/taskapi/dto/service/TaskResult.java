package com.gbm.taskapi.dto.service;

import com.gbm.taskapi.model.TaskPriority;
import com.gbm.taskapi.model.TaskStatus;
import java.time.Instant;

public record TaskResult(
        Long id,
        String title,
        String description,
        ProjectInfo project,
        UserInfo assignee,
        TaskStatus status,
        TaskPriority priority,
        Instant dueDate,
        Instant createdAt,
        int commentCount) {

    public record ProjectInfo(Long id, String name, String description) {}

    public record UserInfo(Long id, String email, String firstName, String lastName, String fullName) {}
}
