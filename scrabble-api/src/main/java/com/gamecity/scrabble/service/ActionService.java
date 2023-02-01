package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
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
     * @param game       the game
     * @param userId     <code>id</code> of the user
     * @param actionType <code>type</code> of the action
     * @return the added action
     */
    Action add(Game game, Long userId, ActionType actionType);

    /**
     * Whether a new {@link Action action} happened in the {@link Game game}
     * 
     * @param gameId  <code>id</code> of the game
     * @param version the expected version
     * @return true if there is a new action
     */
    boolean hasNewAction(Long gameId, Integer version);

    /**
     * Gets the {@link Action action} in the {@link Game game} by the specified <code>version</code>
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the action
     * @return the action
     */
    Action getAction(Long gameId, Integer version);

    /**
     * Whether 4 turns have been skipped in a row to determine whether to end the game
     * 
     * @param gameId      <code>id</code> of the game
     * @param playerCount number of players in the game
     * @return whether the skip count to end the game has been reached
     */
    boolean isMaximumSkipCountReached(Long gameId, Integer playerCount);

}
