package com.board.entity.enums;

/**
 * Categories for retrospective sticky notes.
 * Supports multiple retrospective formats.
 */
public enum RetroCategory {

    // Start/Stop/Continue format
    /**
     * Things to start doing.
     */
    START,

    /**
     * Things to stop doing.
     */
    STOP,

    /**
     * Things to continue doing.
     */
    CONTINUE,

    // Mad/Sad/Glad format
    /**
     * Things that made us mad/frustrated.
     */
    MAD,

    /**
     * Things that made us sad/disappointed.
     */
    SAD,

    /**
     * Things that made us glad/happy.
     */
    GLAD,

    // 4Ls format
    /**
     * Things we liked.
     */
    LIKED,

    /**
     * Things we learned.
     */
    LEARNED,

    /**
     * Things we lacked.
     */
    LACKED,

    /**
     * Things we longed for.
     */
    LONGED_FOR
}
