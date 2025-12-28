package com.board.repository;

import com.board.entity.Ceremony;
import com.board.entity.Sprint;
import com.board.entity.enums.CeremonyStatus;
import com.board.entity.enums.CeremonyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Ceremony entity operations.
 */
@Repository
public interface CeremonyRepository extends JpaRepository<Ceremony, Long> {

    List<Ceremony> findBySprintOrderByScheduledAtAsc(Sprint sprint);

    Optional<Ceremony> findBySprintAndType(Sprint sprint, CeremonyType type);

    List<Ceremony> findBySprintAndStatus(Sprint sprint, CeremonyStatus status);
}
