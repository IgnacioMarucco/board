package com.board.entity;

import com.board.entity.enums.BoardTemplate;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a project in the Board application.
 * A project contains epics, sprints, board columns, and team members.
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    @Column(name = "project_key", nullable = false, unique = true, length = 10)
    private String key;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_template", nullable = false, length = 30)
    private BoardTemplate boardTemplate;

    @Builder.Default
    @Column(name = "sprint_duration_weeks", nullable = false)
    private Integer sprintDurationWeeks = 2;

    @Builder.Default
    @Column(name = "next_item_number", nullable = false)
    private Long nextItemNumber = 1L;

    @Column(name = "time_zone", nullable = false, length = 50)
    private String timeZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardColumn> columns = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Epic> epics = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Sprint> sprints = new ArrayList<>();

    /**
     * Generates the next work item key and increments the counter.
     *
     * @return the next key in format "KEY-123"
     */
    public String generateNextItemKey() {
        String itemKey = this.key + "-" + this.nextItemNumber;
        this.nextItemNumber++;
        return itemKey;
    }
}
