package com.gamecity.scrabble.validator;

/**
 * Validates objects
 * 
 * @author ekarakus
 */
interface Validator {

    /**
     * Validates whether the object is valid
     * 
     * @param object object to validate
     * @return true if the object is valid
     */
    boolean isValid(Object object);

}
