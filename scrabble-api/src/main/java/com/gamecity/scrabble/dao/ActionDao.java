package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Game;

/**
 * Provides dao operations for {@link Action} entity
 * 
 * @author ekarakus
 */
public interface ActionDao extends BaseDao<Action> {

    /**
     * Gets the {@link List list} of {@link Action actions} in a {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the action list
     */
    List<Action> getActions(Long gameId);

    /**
     * Gets the last {@link Action action} happened in a {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the last action
     */
    Action getLastAction(Long gameId);

    /**
     * Gets the {@link Action action} in the {@link Game game} by the specified <code>version</code>
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the action
     * @return the action
     */
    Action getActionByVersion(Long gameId, Integer version);

}
