package com.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a column in the Kanban board of a project.
 * Columns define the workflow stages and can have WIP limits.
 */
@Entity
@Table(name = "board_columns")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumn extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "wip_limit")
    private Integer wipLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Checks if this column has a WIP limit configured.
     *
     * @return true if wipLimit is set and greater than 0
     */
    public boolean hasWipLimit() {
        return wipLimit != null && wipLimit > 0;
    }
}
