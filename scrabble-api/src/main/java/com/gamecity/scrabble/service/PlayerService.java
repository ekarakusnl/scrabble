package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;

/**
 * Provides services for {@link Player players} in a {@link Game game}
 * 
 * @author ekarakus
 */
public interface PlayerService extends BaseService<Player> {

    /**
     * Adds the {@link Player player} to the {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param userId       <code>id</code> of the user
     * @param playerNumber <code>number</code> of the player
     */
    void add(Long gameId, Long userId, Integer playerNumber);

    /**
     * Removes the {@link Player player} from the {@link Game game}
     * 
     * @param player the player to remove
     */
    void remove(Player player);

    /**
     * Gets the {@link List list} of {@link Player players} in the {@link Game game}
     * 
     * @param gameId
     * @return the list of players
     */
    List<Player> getPlayers(Long gameId);

    /**
     * Gets the {@link Player player} by <code>userId</code>
     * 
     * @param gameId <code>id</code> of the game
     * @param userId <code>id</code> of the user
     * @return the player
     */
    Player loadByUserId(Long gameId, Long userId);

    /**
     * Gets the {@link Player player} by <code>player number</code>
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @return the player
     */
    Player loadByPlayerNumber(Long gameId, Integer playerNumber);

}
