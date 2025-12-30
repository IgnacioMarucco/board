package com.board.repository;

import com.board.entity.Sprint;
import com.board.entity.SprintSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for SprintSnapshot entity operations.
 */
@Repository
public interface SprintSnapshotRepository extends JpaRepository<SprintSnapshot, Long> {

    boolean existsBySprintAndSnapshotDate(Sprint sprint, LocalDate snapshotDate);

    List<SprintSnapshot> findBySprintOrderBySnapshotDateAsc(Sprint sprint);
}
