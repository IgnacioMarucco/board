package com.board.exception;

/**
 * Exception thrown when a user tries to access a resource they don't have
 * permission to.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Creates a new ForbiddenException.
     *
     * @param message the error message
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
