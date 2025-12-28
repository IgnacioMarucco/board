package com.board.mapper;

import com.board.dto.attachment.AttachmentResponse;
import com.board.entity.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Attachment entity.
 */
@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(source = "uploadedBy.id", target = "uploadedByUserId")
    @Mapping(source = "uploadedBy.email", target = "uploadedByUserEmail")
    AttachmentResponse toResponse(Attachment attachment);

    List<AttachmentResponse> toResponseList(List<Attachment> attachments);
}
