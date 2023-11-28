package com.gamecity.scrabble.service.exception;

import com.gamecity.scrabble.service.exception.error.UserError;

/**
 * Exception for {@link UserError user errors}
 * 
 * @author ekarakus
 */
public class UserException extends RuntimeException {

    private static final long serialVersionUID = 3999416875805413556L;

    private int code;
    private String message;

    /**
     * Initializes a {@link UserError user error} message
     * 
     * @param error the user error
     */
    public UserException(UserError error) {
        this.code = error.getCode();
        this.message = error.getMessage();
    }

    /**
     * Returns the error code
     * 
     * @return the error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the error message
     * 
     * @return the error message
     */
    @Override
    public String getMessage() {
        return message;
    }

}
