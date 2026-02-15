package com.gbm.taskapi.dto.request;

import com.gbm.taskapi.model.TaskPriority;
import com.gbm.taskapi.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        @NotNull Long projectId,
        Long assigneeId,
        TaskStatus status,
        TaskPriority priority,
        Instant dueDate) {}
