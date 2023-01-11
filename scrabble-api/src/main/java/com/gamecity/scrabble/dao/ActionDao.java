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

}
