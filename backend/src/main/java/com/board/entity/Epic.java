package com.board.entity;

import com.board.entity.enums.EpicStatus;
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
 * Represents an Epic - a large body of work that spans multiple sprints.
 * Epics contain multiple User Stories and belong to a project.
 */
@Entity
@Table(name = "epics")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Epic extends BaseEntity {

    @Column(name = "item_key", nullable = false, unique = true, length = 20)
    private String key;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EpicStatus status = EpicStatus.BACKLOG;

    @Column(length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Builder.Default
    @OneToMany(mappedBy = "epic", cascade = CascadeType.ALL)
    private List<Story> stories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "epic", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "epic", cascade = CascadeType.ALL)
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Calculates the progress percentage based on completed stories.
     *
     * @return percentage of stories in DONE status (0-100)
     */
    public int getProgressPercentage() {
        if (stories.isEmpty()) {
            return 0;
        }
        long doneCount = stories.stream()
                .filter(story -> story.getStatus().name().equals("DONE"))
                .count();
        return (int) ((doneCount * 100) / stories.size());
    }
}
