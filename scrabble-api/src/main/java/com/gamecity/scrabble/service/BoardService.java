package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.entity.Cell;

/**
 * Provides services for {@link Board boards}
 * 
 * @author ekarakus
 */
public interface BoardService extends BaseService<Board> {

    /**
     * Gets the {@link List list} of {@link Cell cells} for the selected {@link Board board}
     * 
     * @param boardId <code>id</code> of the board
     * @return the cells
     */
    List<Cell> getCells(Long boardId);

}
