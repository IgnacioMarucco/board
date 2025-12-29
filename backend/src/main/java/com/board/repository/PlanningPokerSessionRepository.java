package com.board.repository;

import com.board.entity.PlanningPokerSession;
import com.board.entity.Story;
import com.board.entity.enums.PlanningPokerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for planning poker sessions.
 */
@Repository
public interface PlanningPokerSessionRepository extends JpaRepository<PlanningPokerSession, Long> {

    Optional<PlanningPokerSession> findByStoryAndStatusIn(Story story, List<PlanningPokerStatus> statuses);
}
