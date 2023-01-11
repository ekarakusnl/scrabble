package com.gamecity.scrabble.service.exception;

import java.util.List;
import java.util.stream.IntStream;

import com.gamecity.scrabble.service.exception.error.GameError;

/**
 * Exception for {@link GameError game errors}
 * 
 * @author ekarakus
 */
public class GameException extends RuntimeException {

    private static final long serialVersionUID = -6559506856248884501L;

    private int code;
    private String message;
    private List<String> params;

    /**
     * Initializes a {@link GameError game error} message
     * 
     * @param error the gane error
     */
    public GameException(GameError error) {
        this(error, null);
    }

    /**
     * Initializes a {@link GameError game error} message with error message parameters
     * 
     * @param error  the game error
     * @param params error message parameters
     */
    public GameException(GameError error, List<String> params) {
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
     * @return error code
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
