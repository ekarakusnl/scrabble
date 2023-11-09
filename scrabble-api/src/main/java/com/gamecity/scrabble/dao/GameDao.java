package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Game;

/**
 * Provides dao operations for {@link Game} entity
 * 
 * @author ekarakus
 */
public interface GameDao extends BaseDao<Game> {

    /**
     * Gets the {@link List list} of {@link Game games} by the given criterias ordered by <code>createdDate</code>
     * in ascending mode
     * 
     * @param userId <code>id</code> of the user
     * @param includeUser whether or not to include the given user 
     * @return the games
     */
    List<Game> search(Long userId, boolean includeUser);

}
