package com.board.entity.enums;

/**
 * Status values for User Stories in the workflow.
 */
public enum StoryStatus {

    /**
     * Story is in the product backlog, not yet assigned to a sprint.
     */
    BACKLOG,

    /**
     * Story is ready for development (Definition of Ready met).
     */
    READY,

    /**
     * Story is currently being worked on.
     */
    IN_PROGRESS,

    /**
     * Story is in review (code review, QA, etc.).
     */
    IN_REVIEW,

    /**
     * Story is completed and accepted.
     */
    DONE
}
