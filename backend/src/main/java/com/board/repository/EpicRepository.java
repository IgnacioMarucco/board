package com.board.repository;

import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.enums.EpicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Epic entity operations.
 */
@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {

    Optional<Epic> findByKey(String key);

    List<Epic> findByProject(Project project);

    List<Epic> findByProjectAndStatus(Project project, EpicStatus status);
}
