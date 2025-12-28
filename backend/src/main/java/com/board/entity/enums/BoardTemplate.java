package com.board.entity.enums;

/**
 * Predefined board templates that define initial column structure for projects.
 */
public enum BoardTemplate {

    /**
     * Scrum Basic: Todo, In Progress, Review, Done.
     */
    SCRUM_BASIC,

    /**
     * Development Workflow: Backlog, Design, Development, Testing, Deploy, Done.
     */
    DEVELOPMENT,

    /**
     * Support/Bug Tracking: New, Triaged, In Progress, Waiting, Resolved.
     */
    SUPPORT,

    /**
     * Kanban Simple: Todo, Doing, Done.
     */
    KANBAN_SIMPLE,

    /**
     * Content/Marketing: Ideas, Draft, Review, Approved, Published.
     */
    CONTENT_MARKETING
}
