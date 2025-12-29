package com.board.mapper;

import com.board.dto.story.StoryResponse;
import com.board.entity.Story;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Story entity.
 */
@Mapper(componentModel = "spring")
public interface StoryMapper {

    @Mapping(source = "epic.id", target = "epicId")
    @Mapping(source = "epic.key", target = "epicKey")
    @Mapping(source = "sprint.id", target = "sprintId")
    @Mapping(source = "sprint.name", target = "sprintName")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.email", target = "assigneeEmail")
    @Mapping(source = "boardColumn.id", target = "boardColumnId")
    @Mapping(source = "boardColumn.name", target = "boardColumnName")
    StoryResponse toResponse(Story story);

    List<StoryResponse> toResponseList(List<Story> stories);
}
