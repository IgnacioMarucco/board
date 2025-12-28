package com.board.mapper;

import com.board.dto.ceremony.CeremonyResponse;
import com.board.entity.Ceremony;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Ceremony entity.
 */
@Mapper(componentModel = "spring")
public interface CeremonyMapper {

    @Mapping(source = "sprint.id", target = "sprintId")
    @Mapping(source = "sprint.name", target = "sprintName")
    @Mapping(target = "participantIds", expression = "java(ceremony.getParticipants().stream()"
            + ".map(com.board.entity.User::getId).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "retroItemCount", expression = "java(ceremony.getRetroItems() != null "
            + "? ceremony.getRetroItems().size() : 0)")
    CeremonyResponse toResponse(Ceremony ceremony);

    List<CeremonyResponse> toResponseList(List<Ceremony> ceremonies);
}
