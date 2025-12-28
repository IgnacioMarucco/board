package com.board.repository;

import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Sprint entity operations.
 */
@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectOrderByStartDateDesc(Project project);

    Optional<Sprint> findByProjectAndStatus(Project project, SprintStatus status);

    List<Sprint> findByProjectAndStatusIn(Project project, List<SprintStatus> statuses);
}
