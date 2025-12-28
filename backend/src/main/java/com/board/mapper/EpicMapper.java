package com.board.mapper;

import com.board.dto.epic.EpicResponse;
import com.board.entity.Epic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Epic entity.
 */
@Mapper(componentModel = "spring")
public interface EpicMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.key", target = "projectKey")
    EpicResponse toResponse(Epic epic);

    List<EpicResponse> toResponseList(List<Epic> epics);
}
