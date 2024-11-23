package com.gamecity.scrabble.service;

import java.util.List;
import java.util.Set;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.BoardScanFlag;
import com.gamecity.scrabble.model.ConstructedWord;
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

    /**
     * Scans the given {@link VirtualBoard} to find the words
     * 
     * @param gameId         <code>id</code> of the game
     * @param virtualBoard   the board to scan
     * @param boardScanFlags scan flags
     * @return the words
     */
    List<ConstructedWord> scanWords(Long gameId, VirtualBoard virtualBoard, Set<BoardScanFlag> boardScanFlags);

}
