package com.gamecity.scrabble.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.gamecity.scrabble.entity.Tile;
import com.gamecity.scrabble.model.VirtualCell;

import static com.gamecity.scrabble.Constants.Game.BOARD_SIZE;

/**
 * Board operations for unit tests
 */
public abstract class AbstractBoardTest {

    private static final List<Integer> TRIPLE_WORD_CELLS = Arrays.asList(1, 8, 15, 106, 120, 211, 218, 225);
    private static final List<Integer> DOUBLE_WORD_CELLS = Arrays.asList(17, 29, 33, 43, 49, 57, 65, 71, 113, 155, 161,
            169, 177, 183, 193, 197, 209);

    private static final List<Integer> TRIPLE_LETTER_CELLS = Arrays.asList(21, 25, 77, 81, 85, 89, 137, 141, 145, 149,
            201, 205);
    private static final List<Integer> DOUBLE_LETTER_CELLS = Arrays.asList(4, 12, 37, 39, 46, 53, 60, 93, 97, 99, 103,
            109, 117, 123, 127, 129, 133, 166, 173, 180, 187, 189, 214, 222);

    protected VirtualCell[][] boardMatrix;
    protected static final Map<String, Tile> TILE_MAP = new HashMap<>();

    static {
        TILE_MAP.put("A", Tile.builder().letter("A").count(9).value(1).build());
        TILE_MAP.put("B", Tile.builder().letter("B").count(2).value(3).build());
        TILE_MAP.put("C", Tile.builder().letter("C").count(2).value(3).build());
        TILE_MAP.put("D", Tile.builder().letter("D").count(4).value(2).build());
        TILE_MAP.put("E", Tile.builder().letter("E").count(12).value(1).build());
        TILE_MAP.put("F", Tile.builder().letter("F").count(2).value(4).build());
        TILE_MAP.put("G", Tile.builder().letter("G").count(3).value(2).build());
        TILE_MAP.put("H", Tile.builder().letter("H").count(2).value(4).build());
        TILE_MAP.put("I", Tile.builder().letter("I").count(9).value(1).build());
        TILE_MAP.put("J", Tile.builder().letter("J").count(1).value(8).build());
        TILE_MAP.put("K", Tile.builder().letter("K").count(1).value(5).build());
        TILE_MAP.put("L", Tile.builder().letter("L").count(4).value(1).build());
        TILE_MAP.put("M", Tile.builder().letter("M").count(2).value(3).build());
        TILE_MAP.put("N", Tile.builder().letter("N").count(6).value(1).build());
        TILE_MAP.put("O", Tile.builder().letter("O").count(8).value(1).build());
        TILE_MAP.put("P", Tile.builder().letter("P").count(2).value(3).build());
        TILE_MAP.put("Q", Tile.builder().letter("Q").count(1).value(10).build());
        TILE_MAP.put("R", Tile.builder().letter("R").count(6).value(1).build());
        TILE_MAP.put("S", Tile.builder().letter("S").count(4).value(1).build());
        TILE_MAP.put("T", Tile.builder().letter("T").count(6).value(1).build());
        TILE_MAP.put("U", Tile.builder().letter("U").count(4).value(1).build());
        TILE_MAP.put("V", Tile.builder().letter("V").count(2).value(4).build());
        TILE_MAP.put("W", Tile.builder().letter("W").count(2).value(4).build());
        TILE_MAP.put("X", Tile.builder().letter("X").count(1).value(8).build());
        TILE_MAP.put("Y", Tile.builder().letter("Y").count(2).value(4).build());
        TILE_MAP.put("Z", Tile.builder().letter("Z").count(1).value(10).build());
    }

    protected void createBoardMatrix() {
        boardMatrix = new VirtualCell[BOARD_SIZE][BOARD_SIZE];

        IntStream.range(1, BOARD_SIZE + 1).forEach(rowNumber -> {
            IntStream.range(1, BOARD_SIZE + 1).forEach(columnNumber -> {
                final Integer cellNumber = (rowNumber - 1) * BOARD_SIZE + columnNumber;
                final VirtualCell cell = VirtualCell.builder()
                        .cellNumber(cellNumber)
                        .center(rowNumber == 8 && columnNumber == 8)
                        .columnNumber(columnNumber)
                        .hasBottom(rowNumber != BOARD_SIZE)
                        .hasLeft(columnNumber != 1)
                        .hasRight(columnNumber != BOARD_SIZE)
                        .hasTop(rowNumber != 1)
                        .letterValueMultiplier(getLetterValueMultiplier(cellNumber))
                        .rowNumber(rowNumber)
                        .wordScoreMultiplier(getWordScoreMultiplier(cellNumber))
                        .build();

                boardMatrix[cell.getRowNumber() - 1][cell.getColumnNumber() - 1] = cell;
            });
        });
    }

    protected void createExistingVerticalWord(int startingRow, int startingColumn, String word) {
        int rowNumber = startingRow;

        for (char letter : word.toCharArray()) {
            final VirtualCell virtualCell = boardMatrix[rowNumber - 1][startingColumn - 1];
            virtualCell.setLastPlayed(false);
            virtualCell.setLetter(String.valueOf(letter).toUpperCase());
            virtualCell.setSealed(true);
            virtualCell.setValue(TILE_MAP.get(virtualCell.getLetter()).getValue());

            rowNumber = rowNumber + 1;
        }
    }

    protected void createExistingHorizontalWord(int startingRow, int startingColumn, String word) {
        int columnNumber = startingColumn;

        for (char letter : word.toCharArray()) {
            final VirtualCell virtualCell = boardMatrix[startingRow - 1][columnNumber - 1];
            virtualCell.setLastPlayed(false);
            virtualCell.setLetter(String.valueOf(letter).toUpperCase());
            virtualCell.setSealed(true);
            virtualCell.setValue(TILE_MAP.get(virtualCell.getLetter()).getValue());

            columnNumber = columnNumber + 1;
        }
    }

    protected Integer getLetterValueMultiplier(Integer cellNumber) {
        return TRIPLE_LETTER_CELLS.contains(cellNumber) ? 3 : DOUBLE_LETTER_CELLS.contains(cellNumber) ? 2 : 1;
    }

    protected Integer getWordScoreMultiplier(Integer cellNumber) {
        return TRIPLE_WORD_CELLS.contains(cellNumber) ? 3 : DOUBLE_WORD_CELLS.contains(cellNumber) ? 2 : 1;
    }

}
