package com.board.repository;

import com.board.entity.Project;
import com.board.entity.ProjectMember;
import com.board.entity.User;
import com.board.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProjectMember entity operations.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProject(Project project);

    List<ProjectMember> findByProjectAndDeletedAtIsNull(Project project);

    List<ProjectMember> findByUser(User user);

    List<ProjectMember> findByUserAndDeletedAtIsNull(User user);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    Optional<ProjectMember> findByProjectAndUserAndDeletedAtIsNull(Project project, User user);

    boolean existsByProjectAndUser(Project project, User user);

    boolean existsByProjectAndUserAndDeletedAtIsNull(Project project, User user);

    List<ProjectMember> findByProjectAndRole(Project project, Role role);

    List<ProjectMember> findByProjectAndRoleAndDeletedAtIsNull(Project project, Role role);
}
