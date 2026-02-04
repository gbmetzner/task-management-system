package com.gbm.taskapi.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "project_members", schema = "tms")
@Data
public class ProjectMember {

  @EmbeddedId private ProjectMemberId id;

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
}
