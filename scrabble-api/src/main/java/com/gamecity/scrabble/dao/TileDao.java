package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Tile;

/**
 * Provides dao operations for {@link Tile} entity
 * 
 * @author ekarakus
 */
public interface TileDao extends BaseDao<Tile> {

    /**
     * Gets the {@link List list} of the {@link Tile tiles} in a {@link Bag bag}
     * 
     * @param language of the bag
     * @return list of the tiles
     */
    List<Tile> getTiles(Language language);

}
