package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualRack;

/**
 * Updates the components of a {@link Game game} such as {@link VirtualRack rack} and {@link VirtualBoard
 * board}
 * 
 * @author ekarakus
 */
public interface ContentService {

    /**
     * Creates the content of the {@link Game game} data after it starts
     * 
     * @param game       the game that is currently played on
     * @param actionType <code>type</code> of the action happened
     */
    void create(Game game, ActionType actionType);

    /**
     * Updates the content of a {@link Game game} after each {@link Player player} turn
     * 
     * @param game         the game that is currently played on
     * @param updatedRack  updated rack of the player
     * @param updatedBoard updated board of the game
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round
     */
    void update(Game game, VirtualRack updatedRack, VirtualBoard updatedBoard, Integer playerNumber,
            Integer roundNumber);

}
