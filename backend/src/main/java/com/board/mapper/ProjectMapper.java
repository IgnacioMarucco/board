package com.board.mapper;

import com.board.dto.project.ProjectMemberResponse;
import com.board.dto.project.ProjectResponse;
import com.board.entity.Project;
import com.board.entity.ProjectMember;
import com.board.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Project and ProjectMember entities.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "owner", target = "owner")
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);

    @Mapping(source = "owner.id", target = "id")
    @Mapping(source = "owner.email", target = "email")
    @Mapping(source = "owner.firstName", target = "firstName")
    @Mapping(source = "owner.lastName", target = "lastName")
    ProjectResponse.OwnerInfo toOwnerInfo(User owner);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    ProjectMemberResponse toMemberResponse(ProjectMember member);

    List<ProjectMemberResponse> toMemberResponseList(List<ProjectMember> members);
}
