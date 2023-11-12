package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.VirtualBoard;

/**
 * Provides services for {@link VirtualBoard boards} in {@link Game games}
 * 
 * @author ekarakus
 */
public interface VirtualBoardService {

    /**
     * Creates a {@link VirtualBoard board} for the given {@link Game game}
     * 
     * @param gameId  <code>id</code> of the game
     * @param boardId <code>id</code> of the board used in the game
     */
    void createBoard(Long gameId);

    /**
     * Updates the {@link VirtualBoard board} for the given {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param virtualBoard the virtual board to update
     */
    void updateBoard(Long gameId, VirtualBoard virtualBoard);

    /**
     * Returns the {@link VirtualBoard board} for the given {@link Game game}
     * 
     * @param gameId  <code>id</code> of the game
     * @param version <code>version</code> of the board
     * @return the board
     */
    VirtualBoard getBoard(Long gameId, Integer version);

}
