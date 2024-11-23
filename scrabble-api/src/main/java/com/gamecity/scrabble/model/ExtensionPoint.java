package com.gamecity.scrabble.model;

/**
 * Extension of a char sequence
 */
public enum ExtensionPoint {

    /**
     * A new word is going to be created by adding letters after the char sequence
     */
    FORWARD,

    /**
     * A new word is going to be created by adding letters before the char sequence
     */
    BACKWARD,

    /**
     * A new word is going to be created by adding letters in the middle of the char sequence
     */
    MIDDLE,

    /**
     * A new word is going to be created by adding letters to the both sides of the char sequence
     */
    BOTH_SIDES;
}