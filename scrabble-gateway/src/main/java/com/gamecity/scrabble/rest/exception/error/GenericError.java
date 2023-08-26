package com.gamecity.scrabble.rest.exception.error;

/**
 * System errors
 * 
 * @author ekarakus
 */
public enum GenericError {

    /**
     * Profile picture update operation is failed
     */
    PROFILE_PICTURE_UPDATE_FAILED(0001, "Profile picture update operation is failed: {0}");

    private int code;
    private String message;

    private GenericError(int code, String message) {
        this.code = code;
        this.message = message;
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

}
