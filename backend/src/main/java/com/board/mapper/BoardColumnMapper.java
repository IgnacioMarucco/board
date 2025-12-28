package com.board.mapper;

import com.board.dto.boardcolumn.BoardColumnResponse;
import com.board.entity.BoardColumn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for BoardColumn entity.
 */
@Mapper(componentModel = "spring")
public interface BoardColumnMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.key", target = "projectKey")
    BoardColumnResponse toResponse(BoardColumn boardColumn);

    List<BoardColumnResponse> toResponseList(List<BoardColumn> boardColumns);
}
