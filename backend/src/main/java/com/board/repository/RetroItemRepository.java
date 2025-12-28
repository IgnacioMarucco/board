package com.board.repository;

import com.board.entity.Ceremony;
import com.board.entity.RetroItem;
import com.board.entity.enums.RetroCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RetroItem entity operations.
 */
@Repository
public interface RetroItemRepository extends JpaRepository<RetroItem, Long> {

    List<RetroItem> findByCeremonyOrderByVotesDesc(Ceremony ceremony);

    List<RetroItem> findByCeremonyOrderByVotesDescCreatedAtAsc(Ceremony ceremony);

    List<RetroItem> findByCeremonyAndCategory(Ceremony ceremony, RetroCategory category);
}
