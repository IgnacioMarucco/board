package com.board.repository;

import com.board.entity.Ceremony;
import com.board.entity.RetroActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for retro action items.
 */
@Repository
public interface RetroActionItemRepository extends JpaRepository<RetroActionItem, Long> {

    List<RetroActionItem> findByCeremonyOrderByCreatedAtAsc(Ceremony ceremony);
}
