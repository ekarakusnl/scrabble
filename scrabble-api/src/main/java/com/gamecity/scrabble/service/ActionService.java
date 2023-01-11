package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.GameStatus;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.model.VirtualBoard;

/**
 * Provides services for {@link Action actions} happened in the game. This may cover {@link Player
 * player} actions, {@link Word word} actions or actions in the {@link VirtualBoard board}
 * 
 * @author ekarakus
 */
public interface ActionService {

    /**
     * Adds an {@link Action action} item after the last happened action in the {@link Game game}
     * 
     * @param gameId              <code>id</code> of the game
     * @param userId              <code>id</code> of the user
     * @param counter             <code>counter</code> of the action
     * @param currentPlayerNumber <code>number</code> of the current player
     * @param roundNumber         <code>number</code> of the round played
     * @param actionType          <code>type</code> of the action
     * @param status              <code>status</code> of the game
     * @return the added action
     */
    Action add(Long gameId, Long userId, Integer counter, Integer currentPlayerNumber, Integer roundNumber,
            ActionType actionType, GameStatus status);

    /**
     * Whether a new {@link Action action} happened in the {@link Game game}
     * 
     * @param gameId  <code>id</code> of the game
     * @param counter incremental counter of the action count
     * @return true if there is a new action
     */
    boolean hasNewAction(Long gameId, Integer counter);

}
