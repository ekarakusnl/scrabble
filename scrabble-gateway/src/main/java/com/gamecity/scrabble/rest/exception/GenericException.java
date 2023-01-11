package com.gamecity.scrabble.rest.exception;

import com.gamecity.scrabble.model.rest.ExceptionDto;

/**
 * Exception for uncategorized errors
 * 
 * @author ekarakus
 */
public class GenericException extends RuntimeException {

    private static final long serialVersionUID = -2040621966265799628L;

    private ExceptionDto exceptionDto;

    /**
     * Creates a generic error message
     * 
     * @param message the error message
     */
    public GenericException(String message) {
        super(message);
    }

    /**
     * Creates a generic errpr based on {@link ExceptionDto}
     * 
     * @param exceptionDto the exception dto
     */
    public GenericException(ExceptionDto exceptionDto) {
        this.exceptionDto = exceptionDto;
    }

    /**
     * Returns exception dto
     * 
     * @return exception dto
     */
    public ExceptionDto getExceptionDto() {
        return this.exceptionDto;
    }

}
