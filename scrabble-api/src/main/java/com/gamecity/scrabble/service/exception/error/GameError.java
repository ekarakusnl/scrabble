package com.gamecity.scrabble.service.exception.error;

import com.gamecity.scrabble.entity.Game;

/**
 * {@link Game} errors
 * 
 * @author ekarakus
 */
public enum GameError {

    /**
     * Game is not found
     */
    NOT_FOUND(2001, "Game is not found!"),

    /**
     * The player is already in the game
     */
    IN_THE_GAME(2002, "You are already in the game!"),

    /**
     * Game owner cannot leave the game
     */
    OWNER_CANNOT_LEAVE(2003, "Game owner cannot leave the game!"),

    /**
     * The player is not in the game
     */
    NOT_IN_THE_GAME(2004, "You are not in the game!"),

    /**
     * The game has not been started
     */
    NOT_STARTED(2005, "The game has not been started yet!"),

    /**
     * The game is in progress
     */
    IN_PROGRESS(2006, "The game is in progress!"),

    /**
     * Turn of another player
     */
    TURN_OF_ANOTHER_PLAYER(2007, "It is the turn of another player!"),

    /**
     * The rack does not match
     */
    RACK_DOES_NOT_MATCH(2008, "The rack does not match!"),

    /**
     * Center must be non-empty
     */
    CENTER_CANNOT_BE_EMPTY(2009, "Center of the board cannot be empty!"),

    /**
     * Selected cell is not empty
     */
    CELL_IS_NOT_EMPTY(2010, "Cell [{0},{1}] is not empty!"),

    /**
     * Words are not linked to the existing words
     */
    WORDS_ARE_NOT_LINKED(2011, "Words {0} are not linked to any existing words!"),

    /**
     * Words are not found in the dictionary
     */
    WORDS_ARE_NOT_FOUND(2012, "Words {0} are not defined in {1} language!"),

    /**
     * The game is still waiting players
     */
    WAITING(2013, "Game is waiting players!"),

    /**
     * The rack is not full to exchange tiles
     */
    RACK_IS_NOT_FULL(2014, "The rack is not full, you cannot exchange the tiles!"),

    /**
     * Single letter words are not allowed
     */
    SINGLE_LETTER_WORDS_NOT_ALLOWED(2015, "Single letter words {0} are not allowed!"),

    /**
     * There are less tiles in the bag than the exchanged tiles
     */
    INSUFFICIENT_TILES(2016, "There are less tiles in the bag than the exchanged tiles!"),

    /**
     * Game cannot be updated after an action happens in the game
     */
    CANNOT_UPDATE_GAME(2017, "Game cannot be updated after an action happens in the game!");

    private int code;
    private String message;

    private GameError(int code, String message) {
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
