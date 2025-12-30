package com.board.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a daily snapshot of sprint progress metrics.
 */
@Entity
@Table(name = "sprint_snapshots")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "remaining_points", nullable = false)
    private Integer remainingPoints;

    @Column(name = "total_story_count", nullable = false)
    private Integer totalStoryCount;

    @ElementCollection
    @CollectionTable(name = "sprint_snapshot_column_counts",
            joinColumns = @JoinColumn(name = "snapshot_id"))
    @MapKeyColumn(name = "column_name", length = 100)
    @Column(name = "item_count", nullable = false)
    @lombok.Builder.Default
    private Map<String, Integer> columnCounts = new HashMap<>();
}
