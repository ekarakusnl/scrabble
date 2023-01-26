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
        final VirtualCell[] virtualCells = new VirtualCell[board.getRowSize() * board.getColumnSize()];

        final Map<Integer, Cell> cells =
                boardService.getCells(boardId).stream().collect(Collectors.toMap(Cell::getCellNumber, cell -> cell));

        IntStream.range(1, board.getRowSize() + 1).forEach(rowNumber -> {
            IntStream.range(1, board.getColumnSize() + 1).forEach(columnNumber -> {
                final Cell cell = cells.get((rowNumber - 1) * board.getColumnSize() + columnNumber);
                if (cell == null) {
                    throw new IllegalStateException("Cell [{" + rowNumber + "},{" + columnNumber + "}] is not found!");
                }
                final VirtualCell virtualCell = VirtualCell.builder()
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
                virtualCells[virtualCell.getCellNumber() - 1] = virtualCell;
            });
        });

        final VirtualBoard virtualBoard = new VirtualBoard(Arrays.asList(virtualCells));
        redisRepository.updateBoard(gameId, virtualBoard);
        log.info("Board has been created for game {} with the size {}", gameId, board.getName());
    }

    @Override
    public void updateBoard(Long gameId, VirtualBoard virtualBoard) {
        redisRepository.updateBoard(gameId, virtualBoard);
        log.info("Board has been updated for game {}", gameId);
    }

    @Override
    public VirtualBoard getBoard(Long gameId, Integer version) {
        return redisRepository.getBoard(gameId, version);
    }

}
