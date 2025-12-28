package com.board.mapper;

import com.board.dto.projectmember.ProjectMemberResponse;
import com.board.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for ProjectMember entity.
 */
@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(target = "userName", expression = "java(projectMember.getUser().getFirstName() + \" \" "
            + "+ projectMember.getUser().getLastName())")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.key", target = "projectKey")
    ProjectMemberResponse toResponse(ProjectMember projectMember);

    List<ProjectMemberResponse> toResponseList(List<ProjectMember> projectMembers);
}
