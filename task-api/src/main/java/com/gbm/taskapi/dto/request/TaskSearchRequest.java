package com.gbm.taskapi.dto.request;

import com.gbm.taskapi.model.TaskPriority;
import com.gbm.taskapi.model.TaskStatus;

public record TaskSearchRequest(String keyword, TaskStatus status, TaskPriority priority, Long assigneeId) {}
