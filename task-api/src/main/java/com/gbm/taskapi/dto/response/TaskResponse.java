package com.gbm.taskapi.dto.response;

import com.gbm.taskapi.model.TaskPriority;
import com.gbm.taskapi.model.TaskStatus;
import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        ProjectSummary project,
        UserSummary assignee,
        TaskStatus status,
        TaskPriority priority,
        Instant dueDate,
        Instant createdAt,
        int commentCount) {}
