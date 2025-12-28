package com.board.mapper;

import com.board.dto.task.TaskResponse;
import com.board.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Task entity.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "story.id", target = "storyId")
    @Mapping(source = "story.key", target = "storyKey")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.email", target = "assigneeEmail")
    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);
}
