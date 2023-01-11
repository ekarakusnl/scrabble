package com.gamecity.scrabble.service.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Board;
import com.gamecity.scrabble.entity.Cell;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.service.BoardService;
import com.gamecity.scrabble.service.VirtualBoardService;

import lombok.extern.slf4j.Slf4j;

@Service(value = "virtualBoardService")
@Slf4j
class VirtualBoardServiceImpl implements VirtualBoardService {

    private BoardService boardService;
    private RedisRepository redisRepository;

    @Autowired
    void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    @Autowired
    void setRedisRepository(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void createBoard(Long gameId, Long boardId) {
        final Board board = boardService.get(boardId);
        final VirtualCell[] cells = new VirtualCell[board.getRowSize() * board.getColumnSize()];

        IntStream.range(1, board.getRowSize() + 1).forEach(rowNumber -> {
            IntStream.range(1, board.getColumnSize() + 1).forEach(columnNumber -> {
                final VirtualCell cell = createCell(gameId, boardId, rowNumber, columnNumber, board.getColumnSize());
                cells[cell.getCellNumber() - 1] = cell;
            });
        });

        final VirtualBoard virtualBoard = new VirtualBoard(Arrays.asList(cells));
        redisRepository.updateBoard(gameId, virtualBoard);
        log.debug("Board has been created for game {} with the size {}.", gameId, board.getName());
    }

    /**
     * Creates a {@link VirtualCell cell} on a {@link VirtualBoard board} in a {@link Game game}
     * 
     * @param gameId       <code>id</code> of the game
     * @param boardId      <code>id</code> of the board used by the game to determine board settings
     * @param rowNumber    row number of the cell
     * @param columnNumber column number of the cell
     * @return the created cell
     */
    private VirtualCell createCell(Long gameId, Long boardId, int rowNumber, int columnNumber, int columnSize) {
        final Map<Integer, Cell> cells =
                boardService.getCells(boardId).stream().collect(Collectors.toMap(Cell::getCellNumber, cell -> cell));

        final Cell cell = cells.get((rowNumber - 1) * columnSize + columnNumber);
        if (cell == null) {
            // TODO throw exception
            throw new IllegalStateException();
        }

        return VirtualCell.builder()
                .cellNumber(cell.getCellNumber())
                .center(cell.isCenter())
                .color(cell.getColor())
                .columnNumber(cell.getColumnNumber())
                .hasBottom(cell.isHasBottom())
                .hasLeft(cell.isHasLeft())
                .hasRight(cell.isHasRight())
                .hasTop(cell.isHasTop())
                .letterValueMultiplier(cell.getLetterValueMultiplier())
                .rowNumber(cell.getRowNumber())
                .sealed(false)
                .value(0)
                .wordScoreMultiplier(cell.getWordScoreMultiplier())
                .build();
    }

    @Override
    public void updateBoard(Long gameId, VirtualBoard virtualBoard) {
        redisRepository.updateBoard(gameId, virtualBoard);
        log.debug("Board has been updated for game {}.", gameId);
    }

    @Override
    public VirtualBoard getBoard(Long gameId, Integer actionCounter) {
        return redisRepository.getBoard(gameId, actionCounter);
    }

}
