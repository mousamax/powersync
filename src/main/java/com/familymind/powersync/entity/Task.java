package com.familymind.powersync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity representing a task within a task list.
 * Contains task details, assignment tracking, completion status, and recurrence information.
 */
@Entity
@Table(
    name = "task",
    indexes = {
        @Index(name = "idx_task_list_completed", columnList = "task_list_id, is_completed")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseAuditEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_list_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_task_task_list"),
    referencedColumnName = "id")
    private TaskList taskList;

    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "task_date")
    private LocalDate taskDate;

    @Column(name = "task_time")
    private LocalTime taskTime;

    @Column(name = "task_date_time")
    private ZonedDateTime taskDateTime;

    @Column(name = "recurrence_cron")
    private String recurrenceCron;

    @Column(name = "recurring_parent_task_id")
    private UUID recurringParentTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_task_of_id", foreignKey = @ForeignKey(name = "fk_task_parent_task"))
    private Task parentTask;

    @Column(name = "sub_task_of_id", insertable = false, updatable = false)
    private UUID subTaskOfId;

    @Column(name = "position")
    private Integer position;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "assigned_at")
    private ZonedDateTime assignedAt;

    @Column(name = "completed_by")
    private UUID completedBy;

    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    @PrePersist
    @PreUpdate
    protected void updateTaskListActivity() {
        if (this.taskList != null) {
            this.taskList.setLastActivityAt(ZonedDateTime.now());
        }
    }
}

