package com.board.repository;

import com.board.entity.PlanningPokerSession;
import com.board.entity.PlanningPokerVote;
import com.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for planning poker votes.
 */
@Repository
public interface PlanningPokerVoteRepository extends JpaRepository<PlanningPokerVote, Long> {

    Optional<PlanningPokerVote> findBySessionAndVoter(PlanningPokerSession session, User voter);

    List<PlanningPokerVote> findBySession(PlanningPokerSession session);
}
