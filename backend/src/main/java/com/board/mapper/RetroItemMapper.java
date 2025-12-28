package com.board.mapper;

import com.board.dto.retroitem.RetroItemResponse;
import com.board.entity.RetroItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for RetroItem entity.
 */
@Mapper(componentModel = "spring")
public interface RetroItemMapper {

    @Mapping(source = "ceremony.id", target = "ceremonyId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.email", target = "authorEmail")
    RetroItemResponse toResponse(RetroItem retroItem);

    List<RetroItemResponse> toResponseList(List<RetroItem> retroItems);
}
