package com.board.repository;

import com.board.entity.BoardColumn;
import com.board.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BoardColumn entity operations.
 */
@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {

    List<BoardColumn> findByProjectOrderByPositionAsc(Project project);

    int countByProject(Project project);
}
