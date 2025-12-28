package com.board.repository;

import com.board.entity.Attachment;
import com.board.entity.Epic;
import com.board.entity.Story;
import com.board.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Attachment entity operations.
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByEpic(Epic epic);

    List<Attachment> findByStory(Story story);

    List<Attachment> findByTask(Task task);
}
