package com.gamecity.scrabble.dao;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualRack;

/**
 * Provides methods to read/publish data from/to Redis
 * 
 * @author ekarakus
 */
public interface RedisRepository {

    /**
     * Publish the new {@link Action action} in the {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @param action
     */
    void publishAction(Long gameId, Action action);

    /**
     * Publishes the new {@link Chat chat} in the {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @param chat   chat to publish
     */
    void publishChat(Long gameId, Chat chat);

    /**
     * Updates the {@link VirtualBoard board} in the {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @param board  the board to cache
     */
    void updateBoard(Long gameId, VirtualBoard board);

    /**
     * Gets the {@link VirtualBoard board} in the {@link Game game} by the specified <code>version</code>
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the board
     * @return the board
     */
    VirtualBoard getBoard(Long gameId, Integer version);

    /**
     * Fills the {@link VirtualRack rack} of the {@link Player player} in the {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param rack         the rack to cache
     */
    void fillRack(Long gameId, Integer playerNumber, VirtualRack rack);

    /**
     * Updates the {@link VirtualRack rack} of the {@link Player player} in the {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param version      <code>version</code> of the action
     * @param rack         the rack to cache
     */
    void updateRack(Long gameId, Integer playerNumber, Integer version, VirtualRack rack);

    /**
     * Gets the {@link VirtualRack rack} in the {@link Game game} by the specified <code>version</code>
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round
     * @return the rack
     */
    VirtualRack getRack(Long gameId, Integer playerNumber, Integer roundNumber);

}
