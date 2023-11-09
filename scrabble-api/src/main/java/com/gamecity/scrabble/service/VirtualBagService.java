package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Bag;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.model.VirtualRack;

/**
 * Provides services for {@link Bag bags} in a {@link Game game}
 * 
 * @author ekarakus
 */
public interface VirtualBagService {

    /**
     * Gets and binds the {@link List list} of the {@link Tile tiles} in a {@link Bag bag} at the start of
     * the {@link Game game} to the created game
     * 
     * @param gameId <code>id</code> of the game
     * @param language <code>language</code> of the bag used in the game
     * @return the loaded tiles
     */
    List<Tile> getTiles(Long gameId, Language language);

    /**
     * Updates the {@link List list} of {@link Tile tiles} in a {@link Bag bag} after a {@link Word word} is
     * played or a {@link VirtualRack rack} is initialized
     * 
     * @param gameId <code>id</code> of the game
     * @param tiles  updated list of the tiles
     * @return updated list of the tiles
     */
    List<Tile> updateTiles(Long gameId, List<Tile> tiles);

}
