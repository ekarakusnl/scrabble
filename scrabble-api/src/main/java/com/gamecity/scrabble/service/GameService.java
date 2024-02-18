package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.VirtualRack;

/**
 * Provide services for {@link Game game} play actions and {@link Player player} based game actions
 * 
 * @author ekarakus
 */
public interface GameService extends BaseService<Game> {

    /**
     * Joins the {@link Game game}
     * 
     * @param id     <code>id</code> of the game
     * @param userId <code>id</code> of the user in the game
     * @return the updated entity
     */
    Game join(Long id, Long userId);

    /**
     * Leaves the {@link Game game}
     * 
     * @param id     <code>id</code> of the game
     * @param userId <code>id</code> of the user in the game
     * @return the updated entity
     */
    Game leave(Long id, Long userId);

    /**
     * Starts the {@link Game game}
     * 
     * @param id <code>id</code> of the game
     * @return the updated entity
     */
    Game start(Long id);

    /**
     * Plays the word in the {@link Game game}
     * 
     * @param id          <code>id</code> of the game
     * @param userId      <code>id</code> of the user in the game
     * @param virtualRack player rack
     * @param actionType  <code>type</code> of the action
     * @return the updated entity
     */
    Game play(Long id, Long userId, VirtualRack virtualRack, ActionType actionType);

    /**
     * Ends the {@link Game game}
     * 
     * @param id <code>id</code> of the game
     * @return the updated entity
     */
    Game end(Long id);

    /**
     * Gets the {@link List list} of {@link Game games} by the given criterias
     *
     * @param userId      <code>id</code> of the user
     * @param includeUser whether or not to include the given user
     * @return the games
     */
    List<Game> search(Long userId, boolean includeUser);

    /**
     * Terminates the {@link Game game}
     * 
     * @param id <code>id</code> of the game
     * @return the updated entity
     */
    Game terminate(Long id);

    /**
     * Deletes the {@link Game game}
     * 
     * @param id <code>id</code> of the game
     * @return the deleted game
     */
    Game delete(Long id);

}
