package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gamecity.scrabble.model.VirtualTile;

@ExtendWith(MockitoExtension.class)
abstract class AbstractServiceTest extends AbstractBoardTest {

    protected static final Long DEFAULT_ACTION_ID = 1L;
    protected static final String DEFAULT_BAG_LANGUAGE = "en";
    protected static final Long DEFAULT_GAME_ID = 1L;
    protected static final Integer DEFAULT_VERSION = 1;
    protected static final Integer DEFAULT_DURATION = 1;
    protected static final Integer DEFAULT_ROUND_NUMBER = 1;
    protected static final Integer DEFAULT_REMAINING_TILE_COUNT = 98;

    protected static final Long DEFAULT_USER_ID = 4L;
    protected static final Long ALTERNATIVE_USER_ID = 5L;

    protected static final Integer DEFAULT_PLAYER_NUMBER = 1;
    protected static final Integer ALTERNATIVE_PLAYER_NUMBER = 2;

    protected List<VirtualTile> tiles;

    protected void createNewVerticalWord(int startingRow, int startingColumn, String word) {
        int tileNumber = tiles.size() + 1;

        int rowNumber = startingRow;

        for (char letter : word.toCharArray()) {
            tiles.add(VirtualTile.builder()
                    .columnNumber(startingColumn)
                    .letter(String.valueOf(letter).toUpperCase())
                    .number(tileNumber++)
                    .playerNumber(DEFAULT_PLAYER_NUMBER)
                    .rowNumber(rowNumber)
                    .sealed(true)
                    .value(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue())
                    .build());

            rowNumber = rowNumber + 1;
        }
    }

    protected void createNewHorizontalWord(int startingRow, int startingColumn, String word) {
        int tileNumber = tiles.size() + 1;

        int columnNumber = startingColumn;

        for (char letter : word.toCharArray()) {
            tiles.add(VirtualTile.builder()
                    .columnNumber(columnNumber)
                    .letter(String.valueOf(letter).toUpperCase())
                    .number(tileNumber++)
                    .playerNumber(DEFAULT_PLAYER_NUMBER)
                    .rowNumber(startingRow)
                    .sealed(true)
                    .value(TILE_MAP.get(String.valueOf(letter).toUpperCase()).getValue())
                    .build());

            columnNumber = columnNumber + 1;
        }
    }

}
