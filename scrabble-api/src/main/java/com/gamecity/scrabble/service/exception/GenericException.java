package com.gamecity.scrabble.service.exception;

/**
 * Exception for uncategorized errors
 * 
 * @author ekarakus
 */
public class GenericException extends RuntimeException {

    private static final long serialVersionUID = -4060335356623135469L;

    /**
     * Initializes a generic error message
     * 
     * @param message the error message
     */
    public GenericException(String message) {
        super(message);
    }

}
