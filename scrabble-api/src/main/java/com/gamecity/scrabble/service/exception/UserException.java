package com.gamecity.scrabble.service.exception;

import java.util.List;
import java.util.stream.IntStream;

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
    private List<String> params;

    /**
     * Initializes a {@link UserError user error} message
     * 
     * @param error the user error
     */
    public UserException(UserError error) {
        this(error, null);
    }

    /**
     * Initializes a {@link UserError user error} message
     * 
     * @param error  the user error
     * @param params error message parameters
     */
    public UserException(UserError error, List<String> params) {
        this.code = error.getCode();
        this.message = error.getMessage();
        this.params = params;
        if (params != null) {
            IntStream.range(0, params.size())
                    .forEach(index -> message = message.replace("{" + index + "}", params.get(index)));
        }
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
    public String getMessage() {
        return message;
    }

    /**
     * Returns error message parameters
     * 
     * @return error message parameters
     */
    public List<String> getParams() {
        return params;
    }

}
