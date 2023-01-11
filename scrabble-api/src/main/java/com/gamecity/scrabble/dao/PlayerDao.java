package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;

/**
 * Provides dao operations for {@link Player} entity
 * 
 * @author ekarakus
 */
public interface PlayerDao extends BaseDao<Player> {

    /**
     * Gets the {@link Player player} in the {@link Game game} by <code>userId</code>
     * 
     * @param gameId <code>id</code> of the game
     * @param userId <code>id</code> of user in the game
     * @return the player
     */
    Player getByUserId(Long gameId, Long userId);

    /**
     * Gets the {@link Player player} in the {@link Game game} by <code>playerNumber</code>
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player in the game
     * @return the player
     */
    Player getByPlayerNumber(Long gameId, Integer playerNumber);

    /**
     * Gets the {@link List list} of current players in the {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the active players
     */
    List<Player> getCurrentPlayers(Long gameId);

}
