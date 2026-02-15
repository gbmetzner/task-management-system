package com.gbm.taskapi.dto.request;

import com.gbm.taskapi.model.TaskPriority;
import com.gbm.taskapi.model.TaskStatus;
import java.time.Instant;

public record UpdateTaskRequest(
        String title, String description, TaskStatus status, TaskPriority priority, Long assigneeId, Instant dueDate) {}
