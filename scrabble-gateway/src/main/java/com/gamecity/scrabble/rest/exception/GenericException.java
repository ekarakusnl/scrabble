package com.gamecity.scrabble.rest.exception;

import java.util.List;

import com.gamecity.scrabble.model.rest.ExceptionDto;
import com.gamecity.scrabble.model.rest.ExceptionType;

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
     * Creates a generic error message
     * 
     * @param errorCode <code> of the error
     * @param message the error message
     * @param params message parameters
     */
    public GenericException(int errorCode, String message, List<String> params) {
        super(message);
        this.exceptionDto = new ExceptionDto(errorCode, message, params, ExceptionType.SYSTEM);
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
