package com.board.entity.enums;

/**
 * Status values for Sprint lifecycle.
 */
public enum SprintStatus {

    /**
     * Sprint is being planned - stories can be added.
     */
    PLANNING,

    /**
     * Sprint is active - work in progress, scope locked.
     */
    ACTIVE,

    /**
     * Sprint is completed - read-only, all work done or moved back.
     */
    COMPLETED
}
