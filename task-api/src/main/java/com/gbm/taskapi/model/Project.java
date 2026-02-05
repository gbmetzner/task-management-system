package com.gbm.taskapi.model;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "projects", schema = "tms")
@Data
public class Project {
    @Id
    @Tsid
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "name", nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "description", nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false, columnDefinition = "BIGINT")
    private User owner;

    @OneToMany(mappedBy = "project")
    private List<ProjectMember> members;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ProjectStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "project")
    private Set<Task> tasks;
}
