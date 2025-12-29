package com.board.repository;

import com.board.entity.RetroItem;
import com.board.entity.RetroItemVote;
import com.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for retro item votes.
 */
@Repository
public interface RetroItemVoteRepository extends JpaRepository<RetroItemVote, Long> {

    Optional<RetroItemVote> findByRetroItemAndVoter(RetroItem retroItem, User voter);
}
