package com.board.repository;

import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Task entity operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByKey(String key);

    List<Task> findByStoryOrderByPositionAsc(Story story);

    List<Task> findByAssignee(User assignee);

    List<Task> findByStoryAndStatus(Story story, TaskStatus status);
}
