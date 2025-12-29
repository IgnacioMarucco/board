package com.board.repository;

import com.board.entity.BoardColumn;
import com.board.entity.Epic;
import com.board.entity.Sprint;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.StoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Story entity operations.
 */
@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    Optional<Story> findByKey(String key);

    List<Story> findByEpic(Epic epic);

    List<Story> findBySprint(Sprint sprint);

    List<Story> findBySprintAndBoardColumnOrderByPositionAsc(Sprint sprint, BoardColumn column);

    int countBySprintAndBoardColumnAndDeletedAtIsNull(Sprint sprint, BoardColumn column);

    List<Story> findByAssignee(User assignee);

    List<Story> findBySprintAndStatus(Sprint sprint, StoryStatus status);

    List<Story> findBySprintIsNullAndEpic(Epic epic);

    @Query("SELECT s FROM Story s WHERE s.epic.project = :project ORDER BY s.position ASC")
    List<Story> findByEpicProjectOrderByPositionAsc(@Param("project") com.board.entity.Project project);

    @Query("SELECT s FROM Story s WHERE s.epic.project = :project AND s.sprint IS NULL "
            + "ORDER BY s.position ASC")
    List<Story> findByEpicProjectAndSprintIsNullOrderByPositionAsc(
            @Param("project") com.board.entity.Project project);
}
