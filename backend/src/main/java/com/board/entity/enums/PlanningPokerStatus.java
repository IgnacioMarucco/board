package com.board.entity.enums;

/**
 * Status values for planning poker sessions.
 */
public enum PlanningPokerStatus {

    /**
     * Session is open for voting.
     */
    OPEN,

    /**
     * Votes have been revealed to the team.
     */
    REVEALED,

    /**
     * Session is closed and points were finalized.
     */
    CLOSED
}
