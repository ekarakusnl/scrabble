package com.gamecity.scrabble.entity;

/**
 * Type of {@link Action actions} in a {@link Game game}
 * 
 * @author ekarakus
 */
public enum ActionType {

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
     * The turn is skipped
     */
    SKIP,

    /**
     * Game is ended
     */
    END,

    /**
     * Game is terminated
     */
    TERMINATE

}
