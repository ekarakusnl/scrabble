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
     * The turn is skipped due to exchanging letters
     */
    EXCHANGE,

    /**
     * The player used all tiles (7) in the rack to create a single word
     */
    BONUS_BINGO,

    /**
     * The turn is skipped due to play duration timeout
     */
    TIMEOUT,

    /**
     * Game is ended
     */
    END,

    /**
     * Game is deleted
     */
    DELETE,

    /**
     * Game is terminated
     */
    TERMINATE

}
