package com.board.repository;

import com.board.entity.Attachment;
import com.board.entity.enums.AttachmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Attachment entity.
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Finds attachments by entity type and ID, ordered by creation date descending.
     *
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @return list of attachments
     */
    List<Attachment> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            AttachmentType entityType, Long entityId);
}
