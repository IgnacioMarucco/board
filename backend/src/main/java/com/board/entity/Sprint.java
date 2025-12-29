package com.board.entity;

import com.board.entity.enums.SprintStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Sprint in a project.
 * Sprints have fixed duration and contain committed stories.
 */
@Entity
@Table(name = "sprints")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Sprint extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String goal;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "capacity_points")
    private Integer capacityPoints;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "committed_points", nullable = false)
    private Integer committedPoints = 0;

    @Builder.Default
    @Column(name = "committed_story_count", nullable = false)
    private Integer committedStoryCount = 0;

    @Builder.Default
    @Column(name = "completed_points", nullable = false)
    private Integer completedPoints = 0;

    @Builder.Default
    @Column(name = "completed_story_count", nullable = false)
    private Integer completedStoryCount = 0;

    @Builder.Default
    @Column(name = "spillover_count", nullable = false)
    private Integer spilloverCount = 0;

    @Builder.Default
    @Column(name = "scope_change_count", nullable = false)
    private Integer scopeChangeCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SprintStatus status = SprintStatus.PLANNING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder.Default
    @OneToMany(mappedBy = "sprint")
    private List<Story> stories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL)
    private List<Ceremony> ceremonies = new ArrayList<>();

    /**
     * Checks if the sprint is currently active (status-wise).
     *
     * @return true if status is ACTIVE
     */
    public boolean isSprintActive() {
        return status == SprintStatus.ACTIVE;
    }

    /**
     * Checks if the sprint is completed.
     *
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == SprintStatus.COMPLETED;
    }
}
