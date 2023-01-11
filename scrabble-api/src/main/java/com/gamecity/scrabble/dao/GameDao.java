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
     * Gets the {@link List list} of {@link Game games} by maximum count ordered by <code>createdDate</code>
     * in descending mode
     * 
     * @param count number of games to fetch
     * @return the last games
     */
    List<Game> getLastGames(int count);

}
