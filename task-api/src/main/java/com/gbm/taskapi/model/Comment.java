package com.gbm.taskapi.model;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "comments", schema = "tms")
@Data
public class Comment {
  @Id
  @Tsid
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BIGINT")
  private Long id;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @ManyToOne
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime updatedAt;
}
