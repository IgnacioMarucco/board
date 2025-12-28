package com.board.mapper;

import com.board.dto.sprint.SprintResponse;
import com.board.entity.Sprint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Sprint entity.
 */
@Mapper(componentModel = "spring")
public interface SprintMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.key", target = "projectKey")
    SprintResponse toResponse(Sprint sprint);

    List<SprintResponse> toResponseList(List<Sprint> sprints);
}
