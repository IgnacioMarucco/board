package com.board.mapper;

import com.board.dto.activity.ActivityLogResponse;
import com.board.entity.ActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for ActivityLog entity.
 */
@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(source = "actor.id", target = "actorId")
    @Mapping(source = "actor.username", target = "actorUsername")
    ActivityLogResponse toResponse(ActivityLog activityLog);

    List<ActivityLogResponse> toResponseList(List<ActivityLog> activityLogs);
}
