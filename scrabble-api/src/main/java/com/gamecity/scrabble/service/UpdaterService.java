package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualScoreboard;

/**
 * Updates the content of a {@link Game game} such as {@link VirtualScoreboard score board} update,
 * {@link VirtualRack rack} update or{@link VirtualBoard board} update
 * 
 * @author ekarakus
 */
public interface UpdaterService {

    /**
     * Runs the update after the specified action in the {@link Game game}
     * 
     * @param game       the game that is currently played on
     * @param actionType <code>type</code> of the action happened
     */
    void run(Game game, ActionType actionType);

    /**
     * Runs the update after the specified action in the {@link Game game}
     * 
     * @param game         the game that is currently played on
     * @param updatedRack  updated rack of the player
     * @param updatedBoard updated board of the game
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round
     */
    void run(Game game, VirtualRack updatedRack, VirtualBoard updatedBoard, Integer playerNumber, Integer roundNumber);

}
