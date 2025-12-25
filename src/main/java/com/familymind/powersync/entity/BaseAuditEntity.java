package com.familymind.powersync.entity;

import com.familymind.powersync.util.UuidV7;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Base entity class providing common audit fields for all entities.
 * This is an ORM-level inheritance only (not database-level).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseAuditEntity {

    @Id
    @UuidV7
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "creator_id")
    private UUID creatorId;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        // TODO: Get from Spring Security context when authentication is implemented
        // this.creatorId = getCurrentUserId();
        // this.updatedBy = getCurrentUserId();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
        // TODO: Get from Spring Security context when authentication is implemented
        // this.updatedBy = getCurrentUserId();
    }
}

