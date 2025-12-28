package com.board.exception;

/**
 * Exception thrown when a request contains invalid data.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Creates a new BadRequestException.
     *
     * @param message the error message
     */
    public BadRequestException(String message) {
        super(message);
    }
}
