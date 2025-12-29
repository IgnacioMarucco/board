package com.board.entity;

import com.board.entity.enums.CeremonyStatus;
import com.board.entity.enums.CeremonyType;
import com.board.entity.enums.RetroTemplate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Scrum ceremony (Planning, Daily, Review, Retrospective,
 * Refinement).
 * Ceremonies are associated with a Sprint and can have participants and notes.
 */
@Entity
@Table(name = "ceremonies")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Ceremony extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CeremonyType type;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CeremonyStatus status = CeremonyStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "retro_template", length = 30)
    private RetroTemplate retroTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "ceremony_participants",
        joinColumns = @JoinColumn(name = "ceremony_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "ceremony", cascade = CascadeType.ALL)
    private List<RetroItem> retroItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ceremony", cascade = CascadeType.ALL)
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Checks if this ceremony is a retrospective.
     *
     * @return true if type is RETROSPECTIVE
     */
    public boolean isRetrospective() {
        return type == CeremonyType.RETROSPECTIVE;
    }
}
