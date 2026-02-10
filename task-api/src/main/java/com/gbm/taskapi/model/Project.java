package com.gbm.taskapi.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "projects", schema = "tms")
@Getter
@Setter
public class Project extends BaseEntity {

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "VARCHAR(255)")
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false, columnDefinition = "BIGINT")
    private User owner;

    @OneToMany(mappedBy = "project")
    private List<ProjectMember> members;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "PROJECT_STATUS")
    private ProjectStatus status;

    @OneToMany(mappedBy = "project")
    private Set<Task> tasks;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Project project)) return false;
        return Objects.equals(getId(), project.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
