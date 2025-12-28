package com.board.service;

import com.board.dto.ceremony.CeremonyCreateRequest;
import com.board.dto.ceremony.CeremonyResponse;
import com.board.dto.ceremony.CeremonyUpdateRequest;

import java.util.List;

/**
 * Service interface for ceremony operations.
 */
public interface CeremonyService {

    /**
     * Creates a ceremony for a sprint.
     *
     * @param sprintId the sprint ID
     * @param request  the create request
     * @param userId   the requesting user's ID
     * @return the created ceremony
     */
    CeremonyResponse createCeremony(Long sprintId, CeremonyCreateRequest request, Long userId);

    /**
     * Gets a ceremony by ID.
     *
     * @param ceremonyId the ceremony ID
     * @param userId     the requesting user's ID
     * @return the ceremony
     */
    CeremonyResponse getCeremony(Long ceremonyId, Long userId);

    /**
     * Gets all ceremonies for a sprint.
     *
     * @param sprintId the sprint ID
     * @param userId   the requesting user's ID
     * @return list of ceremonies
     */
    List<CeremonyResponse> getCeremoniesForSprint(Long sprintId, Long userId);

    /**
     * Updates a ceremony.
     *
     * @param ceremonyId the ceremony ID
     * @param request    the update request
     * @param userId     the requesting user's ID
     * @return the updated ceremony
     */
    CeremonyResponse updateCeremony(Long ceremonyId, CeremonyUpdateRequest request, Long userId);

    /**
     * Deletes a ceremony.
     *
     * @param ceremonyId the ceremony ID
     * @param userId     the requesting user's ID
     */
    void deleteCeremony(Long ceremonyId, Long userId);
}
