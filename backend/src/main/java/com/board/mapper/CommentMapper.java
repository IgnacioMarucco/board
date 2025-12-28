package com.board.mapper;

import com.board.dto.comment.CommentResponse;
import com.board.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Comment entity.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.email", target = "authorEmail")
    @Mapping(source = "epic.id", target = "epicId")
    @Mapping(source = "story.id", target = "storyId")
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    @Mapping(target = "replyCount", expression = "java(comment.getReplies() != null ? comment.getReplies().size() : 0)")
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);
}
