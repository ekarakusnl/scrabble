package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Bag;
import com.gamecity.scrabble.entity.Tile;

/**
 * Provides services for {@link Bag bags}
 * 
 * @author ekarakus
 */
public interface BagService extends BaseService<Bag> {

    /**
     * Gets the {@link List list} of {@link Tile tiles} for the selected {@link Bag bag}
     * 
     * @param bagId <code>id</code> of the bag
     * @return the tiles
     */
    List<Tile> getTiles(Long bagId);

}
