package com.gamecity.scrabble.entity;

/**
 * Status of a {@link Game}
 * 
 * @author ekarakus
 */
public enum GameStatus {

    /**
     * The game does not have a status
     */
    NO_STATUS,

    /**
     * The game is waiting players to join
     */
    WAITING,

    /**
     * The game is ready to start
     */
    READY_TO_START,

    /**
     * The game is in progress
     */
    IN_PROGRESS,

    /**
     * The game is ready to end
     */
    READY_TO_END,

    /**
     * The game is ended
     */
    ENDED,

    /**
     * The game is deleted
     */
    DELETED,

    /**
     * The game is terminated
     */
    TERMINATED

}
