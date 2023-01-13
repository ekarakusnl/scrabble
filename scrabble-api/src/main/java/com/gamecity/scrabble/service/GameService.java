package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.User;
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
     * @return the updated entity
     */
    Game play(Long id, Long userId, VirtualRack virtualRack);

    /**
     * Ends the {@link Game game}
     * 
     * @param id <code>id</code> of the game
     * @return the updated entity
     */
    Game end(Long id);

    /**
     * Gets the {@link List list} of {@link Game games} where the {@link User user} plays
     * 
     * @param userId <code>id</code> of the user
     * @return the games
     */
    List<Game> listByUser(Long userId);

}
