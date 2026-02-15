package com.gbm.taskapi.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(
        name = "tasks",
        schema = "tms",
        indexes = {
            @Index(name = "idx_task_project_id", columnList = "project_id"),
            @Index(name = "idx_task_assignee_id", columnList = "assignee_id"),
            @Index(name = "idx_task_status", columnList = "status"),
            @Index(name = "idx_task_priority", columnList = "priority"),
            @Index(name = "idx_task_due_date", columnList = "due_date"),
            @Index(name = "idx_task_created_at", columnList = "created_at")
        })
public class Task extends BaseEntity {

    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255)")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "TASK_STATUS")
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "priority", nullable = false, length = 20, columnDefinition = "TASK_PRIORITY")
    private TaskPriority priority;

    @Column(name = "due_date", columnDefinition = "TIMESTAMPTZ")
    private Instant dueDate;

    @Column(name = "completed_at", columnDefinition = "TIMESTAMPTZ")
    private Instant completedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Task task)) return false;
        return Objects.equals(getId(), task.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
