package com.board.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates a new NotFoundException.
     *
     * @param message the error message
     */
    public NotFoundException(String message) {
        super(message);
    }
}
