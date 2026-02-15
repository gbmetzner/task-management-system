package com.gbm.taskapi.dto.response;

import com.gbm.taskapi.model.Project;

public record ProjectSummary(Long id, String name, String description) {
    public ProjectSummary(Project project) {
        this(project.getId(), project.getName(), project.getDescription());
    }
}
