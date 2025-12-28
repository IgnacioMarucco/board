package com.board.exception;

/**
 * Exception thrown when authentication fails.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Creates a new UnauthorizedException.
     *
     * @param message the error message
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
