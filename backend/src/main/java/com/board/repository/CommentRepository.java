package com.board.repository;

import com.board.entity.Comment;
import com.board.entity.Epic;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEpicOrderByCreatedAtDesc(Epic epic);

    List<Comment> findByStoryOrderByCreatedAtDesc(Story story);

    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);

    List<Comment> findByAuthor(User author);

    List<Comment> findByParentComment(Comment parentComment);
}
