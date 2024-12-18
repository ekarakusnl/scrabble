package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;

/**
 * Provides services for {@link VirtualRack virtual racks} in a {@link Game game}
 * 
 * @author ekarakus
 */
public interface VirtualRackService {

    /**
     * Creates the {@link VirtualRack rack} for the {@link Player player} after the {@link Game game} is
     * started
     * 
     * @param gameId       <code>id</code> of the game
     * @param language     <code>language</code> of the bag
     * @param playerNumber <code>number</code> of the player
     */
    void createRack(Long gameId, Language language, Integer playerNumber);

    /**
     * Fills the empty tiles in te rack {@link VirtualRack rack} of the {@link Player player}
     * 
     * @param gameId       <code>id</code> of the game
     * @param language     <code>language</code> of the bag
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round played
     * @param virtualRack  the virtual rack to refresh
     */
    void fillRack(Long gameId, Language language, Integer playerNumber, Integer roundNumber, VirtualRack virtualRack);

    /**
     * Updates the {@link VirtualRack rack} of the {@link Player player} after a {@link Word word} is played
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round played
     * @param virtualRack  the virtual rack to refresh
     */
    void updateRack(Long gameId, Integer playerNumber, Integer roundNumber, VirtualRack virtualRack);

    /**
     * Gets the {@link VirtualRack rack} of the {@link Player player} for the given {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round played
     * @return the rack
     */
    VirtualRack getRack(Long gameId, Integer playerNumber, Integer roundNumber);

    /**
     * Exchanges the selected {@link VirtualTile tiles} in the {@link VirtualRack rack}
     * 
     * @param gameId        <code>id</code> of the game
     * @param language      <code>language</code> of the bag
     * @param playerNumber  <code>number</code> of the player
     * @param roundNumber   <code>number</code> of the round played
     * @param exchangedRack rack with the exchanged tiles
     */
    void exchange(Long gameId, Language language, Integer playerNumber, Integer roundNumber, VirtualRack exchangedRack);

    /**
     * Whether the played {@link VirtualRack rack} of the player is the one that is stored in the system
     * 
     * @param gameId       <code>id</code> of the game
     * @param playerNumber <code>number</code> of the player
     * @param roundNumber  <code>number</code> of the round played
     * @param playedRack   rack with the played tiles
     */
    void validateRack(Long gameId, Integer playerNumber, Integer roundNumber, VirtualRack playedRack);

}
