package com.board.entity;

import com.board.entity.enums.Priority;
import com.board.entity.enums.StoryStatus;
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
import java.util.List;

/**
 * Represents a User Story - a unit of work that delivers value.
 * Stories belong to an Epic and can be assigned to a Sprint.
 */
@Entity
@Table(name = "stories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Story extends BaseEntity {

    @Column(name = "item_key", nullable = false, unique = true, length = 20)
    private String key;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "acceptance_criteria", columnDefinition = "TEXT")
    private String acceptanceCriteria;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoryStatus status = StoryStatus.BACKLOG;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Builder.Default
    @Column(nullable = false)
    private Integer position = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_column_id")
    private BoardColumn boardColumn;

    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL)
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Checks if this story is assigned to a sprint.
     *
     * @return true if sprint is not null
     */
    public boolean isInSprint() {
        return sprint != null;
    }

    /**
     * Checks if this story has been estimated.
     *
     * @return true if storyPoints is set
     */
    public boolean isEstimated() {
        return storyPoints != null;
    }
}
