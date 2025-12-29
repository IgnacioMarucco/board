package com.board.entity;

import com.board.entity.enums.RetroCategory;
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
 * Represents a sticky note item in a Sprint Retrospective.
 * Supports anonymous submissions and voting.
 */
@Entity
@Table(name = "retro_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RetroItem extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RetroCategory category;

    @Builder.Default
    @Column(nullable = false)
    private Integer votes = 0;

    @Builder.Default
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceremony_id", nullable = false)
    private Ceremony ceremony;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Builder.Default
    @OneToMany(mappedBy = "retroItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetroItemVote> voteEntries = new ArrayList<>();

    /**
     * Adds a vote to this retro item.
     */
    public void addVote() {
        this.votes++;
    }

    /**
     * Removes a vote from this retro item (minimum 0).
     */
    public void removeVote() {
        if (this.votes > 0) {
            this.votes--;
        }
    }
}
