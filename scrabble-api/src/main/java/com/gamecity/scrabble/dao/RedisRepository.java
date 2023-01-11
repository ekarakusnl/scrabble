package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Action;
import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualRack;

/**
 * Provides methods to read/publish data from/to Redis cache
 * 
 * @author ekarakus
 */
public interface RedisRepository {

    /**
     * Publish a new {@link Action action} in a {@link Game game} to Redis
     * 
     * @param gameId <code>id</code> of the game
     * @param action
     */
    void publishAction(Long gameId, Action action);

    /**
     * Gets the last {@link Action action} happened in a {@link Game game} by the specified
     * <code>counter</code>
     * 
     * @param gameId  <code>id</code> of the game
     * @param counter <code>counter</code> of the action
     * @return the action
     */
    Action getAction(Long gameId, Integer counter);

    /**
     * Updates the current {@link List list} of {@link Player players} in a {@link Game game} in Redis cache
     * 
     * @param gameId  <code>id</code> of the game
     * @param players the list of players to update
     */
    void updatePlayers(Long gameId, List<Player> players);

    /**
     * Gets the {@link List list} of {@link Player players} in a {@link Game game} after the specified
     * <code>actionCounter</code>
     * 
     * @param gameId        <code>id</code> of the game
     * @param actionCounter <code>counter</code> of the action
     * @return the list of players
     */
    List<Player> getPlayers(Long gameId, Integer actionCounter);

    /**
     * Publishes a new {@link Chat chat} sent in a {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @param chat   chat to publish
     */
    void publishChat(Long gameId, Chat chat);

    /**
     * Gets the {@link List list} of {@link Chat chats} in a {@link Game game} sent by the specified
     * <code>actionCounter</code>
     * 
     * @param gameId        <code>id</code> of the game
     * @param actionCounter <code>counter</code> of the action
     * @return the list of chats
     */
    List<Chat> getChats(Long gameId, Integer actionCounter);

    /**
     * Updates the current {@link VirtualBoard board} in a {@link Game game} in Redis cache
     * 
     * @param gameId <code>id</code> of the game
     * @param board  the board to cache
     */
    void updateBoard(Long gameId, VirtualBoard board);

    /**
     * Gets the {@link VirtualBoard board} in a {@link Game game} cached in Redis by the specified
     * <code>actionCounter</code>
     * 
     * @param gameId        <code>id</code> of the game
     * @param actionCounter <code>counter</code> of the action
     * @return the board
     */
    VirtualBoard getBoard(Long gameId, Integer actionCounter);

    /**
     * Refreshes the current {@link VirtualRack rack} of a {@link Player player} in a {@link Game game} in
     * Redis cache
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param rack         the rack to cache
     */
    void refreshRack(Long gameId, Integer playerNumber, VirtualRack rack);

    /**
     * Updates the current {@link VirtualRack rack} of a {@link Player player} in a {@link Game game} in
     * Redis cache
     * 
     * @param gameId        <code>id</code> of the game
     * @param playerNumber  <code>number</code> of the player
     * @param actionCounter <code>counter</code> of the action
     * @param rack          the rack to cache
     */
    void updateRack(Long gameId, Integer playerNumber, Integer actionCounter, VirtualRack rack);

    /**
     * Gets the {@link VirtualRack rack} in a {@link Game game} cached in Redis by the specified
     * <code>actionCounter</code>
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round
     * @return the rack
     */
    VirtualRack getRack(Long gameId, Integer playerNumber, Integer roundNumber);

}
