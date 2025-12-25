package com.familymind.powersync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

/**
 * Entity representing a task list within a family.
 * Contains list information and sorting preferences.
 */
@Entity
@Table(
    name = "task_list",
    indexes = {
        @Index(name = "idx_task_list_family_id", columnList = "family_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskList extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_task_list_family"),
    referencedColumnName = "id")
    private Family family;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "sort_by")
    private String sortBy = "custom";

    @Column(name = "last_activity_at")
    private ZonedDateTime lastActivityAt;

    @PrePersist
    protected void onTaskListCreate() {
        if (this.lastActivityAt == null) {
            this.lastActivityAt = ZonedDateTime.now();
        }
    }
}

