package com.board.entity.enums;

/**
 * Status values for Epics.
 */
public enum EpicStatus {

    /**
     * Epic is defined but work has not started.
     */
    BACKLOG,

    /**
     * Epic has stories in progress.
     */
    IN_PROGRESS,

    /**
     * All stories in epic are completed.
     */
    DONE
}
