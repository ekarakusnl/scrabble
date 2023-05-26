package com.gamecity.scrabble.entity;

/**
 * Type of {@link Action actions} in a {@link Game game}
 * 
 * @author ekarakus
 */
public enum ActionType {

    /**
     * A player created the game
     */
    CREATE,

    /**
     * A player joined to the game
     */
    JOIN,

    /**
     * A player left the game
     */
    LEAVE,

    /**
     * Game is started
     */
    START,

    /**
     * A word is played
     */
    PLAY,

    /**
     * The turn is skipped by the player
     */
    SKIP,

    /**
     * The turn is skipped due to pkay duration timeout
     */
    TIMEOUT,

    /**
     * Game is ended
     */
    END,

    /**
     * Game is terminated
     */
    TERMINATE

}
