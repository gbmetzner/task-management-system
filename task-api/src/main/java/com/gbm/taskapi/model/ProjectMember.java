package com.gbm.taskapi.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "project_members", schema = "tms")
@Getter
@Setter
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime joinedAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProjectMember projectMember)) return false;
        return Objects.equals(getId(), projectMember.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
