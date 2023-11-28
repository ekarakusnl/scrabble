package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.CellDao;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Cell;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.service.VirtualBoardService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static com.gamecity.scrabble.Constants.Game.BOARD_COLUMN_SIZE;
import static com.gamecity.scrabble.Constants.Game.BOARD_ROW_SIZE;

class TestVirtualBoardService extends AbstractServiceTest {

    @Mock
    private CellDao cellDao;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private VirtualBoardService virtualBoardService = new VirtualBoardServiceImpl(cellDao, redisRepository);

    @Test
    void test_create_board() {
        final List<Cell> cells = new ArrayList<>(BOARD_ROW_SIZE * BOARD_COLUMN_SIZE);
        IntStream.range(1, BOARD_ROW_SIZE + 1).forEach(rowNumber -> {
            IntStream.range(1, BOARD_COLUMN_SIZE + 1).forEach(columnNumber -> {
                cells.add(Cell.builder()
                        .rowNumber(rowNumber)
                        .columnNumber(columnNumber)
                        .cellNumber(cells.size() + 1)
                        .build());
            });
        });

        when(cellDao.list()).thenReturn(cells);

        virtualBoardService.createBoard(DEFAULT_GAME_ID);

        final ArgumentCaptor<VirtualBoard> virtualBoard = ArgumentCaptor.forClass(VirtualBoard.class);

        verify(redisRepository).updateBoard(eq(DEFAULT_GAME_ID), virtualBoard.capture());

        assertThat(virtualBoard.getValue().getCells().size(), equalTo(cells.size()));
    }

    @Test
    void test_create_board_with_missing_cell() {
        final List<Cell> cells = new ArrayList<>(BOARD_ROW_SIZE * BOARD_COLUMN_SIZE);

        when(cellDao.list()).thenReturn(cells);

        try {
            virtualBoardService.createBoard(DEFAULT_GAME_ID);

            fail("The board is created with insufficient cells");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), equalTo("Cell [{1},{1}] is not found!"));
        }
    }

}
