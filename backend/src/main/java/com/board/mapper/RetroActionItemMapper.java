package com.board.mapper;

import com.board.dto.retroactionitem.RetroActionItemResponse;
import com.board.entity.RetroActionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for RetroActionItem entity.
 */
@Mapper(componentModel = "spring")
public interface RetroActionItemMapper {

    @Mapping(source = "ceremony.id", target = "ceremonyId")
    @Mapping(target = "assigneeId", expression = "java(actionItem.getAssignee() != null "
            + "? actionItem.getAssignee().getId() : null)")
    @Mapping(target = "assigneeEmail", expression = "java(actionItem.getAssignee() != null "
            + "? actionItem.getAssignee().getEmail() : null)")
    @Mapping(target = "retroItemId", expression = "java(actionItem.getRetroItem() != null "
            + "? actionItem.getRetroItem().getId() : null)")
    @Mapping(target = "taskId", expression = "java(actionItem.getTask() != null "
            + "? actionItem.getTask().getId() : null)")
    RetroActionItemResponse toResponse(RetroActionItem actionItem);

    List<RetroActionItemResponse> toResponseList(List<RetroActionItem> actionItems);
}
