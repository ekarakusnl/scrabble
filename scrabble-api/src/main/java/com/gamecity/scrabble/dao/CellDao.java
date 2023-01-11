package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.entity.Cell;

/**
 * Provides dao operations for {@link Cell} entity
 * 
 * @author ekarakus
 */
public interface CellDao extends BaseDao<Cell> {

    /**
     * Gets the {@link List list} of the {@link Cell cells} in a {@link Board board}
     * 
     * @param boardId <code>id</code> of the board
     * @return list of the cells
     */
    List<Cell> getCells(Long boardId);

}
