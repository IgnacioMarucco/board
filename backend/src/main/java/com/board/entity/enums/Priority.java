package com.board.entity.enums;

/**
 * Priority levels for work items (Stories and Tasks).
 */
public enum Priority {

    /**
     * Low priority - Can be deferred.
     */
    LOW,

    /**
     * Medium priority - Normal work items.
     */
    MEDIUM,

    /**
     * High priority - Should be addressed soon.
     */
    HIGH,

    /**
     * Critical priority - Must be addressed immediately.
     */
    CRITICAL
}
