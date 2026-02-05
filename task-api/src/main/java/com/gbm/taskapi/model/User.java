package com.gbm.taskapi.model;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users", schema = "tms")
@Data
public class User {

    @Id
    @Tsid
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "email", unique = true, nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String email;

    @Column(name = "password", nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String password;

    @Column(name = "firstname", length = 100, columnDefinition = "VARCHAR(255)")
    private String firstName;

    @Column(name = "lastname", length = 100, columnDefinition = "VARCHAR(255)")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false, columnDefinition = "USER_ROLE")
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "assignee")
    private List<Task> assignedTasks;

    @OneToMany(mappedBy = "member")
    private List<ProjectMember> projectMemberships;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
