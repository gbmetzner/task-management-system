package com.gbm.taskapi.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens", schema = "tms")
@Getter
@Setter
public class RefreshToken extends BaseEntity {
    @Column(name = "token", nullable = false, columnDefinition = "VARCHAR(255)")
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        token = UUID.randomUUID().toString();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
