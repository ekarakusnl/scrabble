package com.gamecity.scrabble.service.exception.error;

import com.gamecity.scrabble.entity.User;

/**
 * {@link User} errors
 * 
 * @author ekarakus
 */
public enum UserError {

    /**
     * User is not found
     */
    NOT_FOUND(1001, "User is not found!"),

    /**
     * Email address is not valid
     */
    EMAIL_ADDRESS_NOT_VALID(1002, "E-mail address is not valid!"),

    /**
     * Email address is in use
     */
    EMAIL_ADDRESS_IN_USE(1003, "E-mail address is used by another user!"),

    /**
     * Username is not valid
     */
    USERNAME_NOT_VALID(1004, "Username can only contain alphabetic characters!"), //

    /**
     * Username is in use
     */
    USERNAME_IN_USE(1005, "Username is used by another user!"),

    /**
     * Password is not strong
     */
    PASSWORD_NOT_STRONG(1006,
            "Password must contain at least 1 capital letter, 1 short letter, 1 number and 1 special character!"),

    /**
     * User account has been disabled
     */
    ACCOUNT_DISABLED(1007, "User has been disabled!"),

    /**
     * User account has been expired
     */
    ACCOUNT_EXPIRED(1008, "User account has been expired!"),

    /**
     * User account has been locked
     */
    ACCOUNT_LOCKED(1009, "User account has been locked!"),

    /**
     * User credentials have been expired
     */
    CREDENTIALS_EXPIRED(1010, "User credentials have been expired!"),

    /**
     * Username cannot be changed
     */
    USERNAME_CANNOT_BE_CHANGED(1011, "Username cannot be changed!"),

    /**
     * Email address cannot be changed
     */
    EMAIL_CANNOT_BE_CHANGED(1012, "Email cannot be changed!");

    private int code;
    private String message;

    private UserError(int code, String message) {
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
