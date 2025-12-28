package com.board.mapper;

import com.board.dto.notification.NotificationResponse;
import com.board.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Notification entity.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "triggeredBy.id", target = "triggeredById")
    @Mapping(source = "triggeredBy.email", target = "triggeredByEmail")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
