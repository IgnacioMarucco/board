package com.board.repository;

import com.board.entity.ActivityLog;
import com.board.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ActivityLog entity operations.
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByProjectAndDeletedAtIsNullOrderByCreatedAtDesc(Project project);
}
