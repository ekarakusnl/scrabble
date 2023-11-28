package com.gamecity.scrabble.service.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.CellDao;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Cell;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.service.VirtualBoardService;

import lombok.extern.slf4j.Slf4j;

import static com.gamecity.scrabble.Constants.Game.BOARD_ROW_SIZE;
import static com.gamecity.scrabble.Constants.Game.BOARD_COLUMN_SIZE;

@Service(value = "virtualBoardService")
@Slf4j
class VirtualBoardServiceImpl implements VirtualBoardService {

    private CellDao cellDao;
    private RedisRepository redisRepository;

    public VirtualBoardServiceImpl(final CellDao cellDao, final RedisRepository redisRepository) {
        this.cellDao = cellDao;
        this.redisRepository = redisRepository;
    }

    @Override
    public void createBoard(Long gameId) {
        final VirtualCell[] virtualCells = new VirtualCell[BOARD_ROW_SIZE * BOARD_COLUMN_SIZE];

        final Map<Integer, Cell> cells = cellDao.list()
                .stream()
                .collect(Collectors.toMap(Cell::getCellNumber, cell -> cell));

        IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
            IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
                final Cell cell = cells.get((rowNumber - 1) * BOARD_COLUMN_SIZE + columnNumber);
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
        log.info("Board has been created for game {}", gameId);
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
