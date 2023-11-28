package com.gamecity.scrabble.service.impl;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import com.gamecity.scrabble.model.Bonus;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.ScoreService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class TestScoreService extends AbstractServiceTest {

    @InjectMocks
    private ScoreService scoreService = new ScoreServiceImpl();

    @Test
    void test_calculate_constructed_word_score_that_uses_empty_word_multiplier_cell() {
        final List<VirtualCell> cells = Arrays.asList(
                VirtualCell.builder().letter("F").lastPlayed(true).value(1).wordScoreMultiplier(2).build(),
                VirtualCell.builder().letter("E").lastPlayed(true).value(2).wordScoreMultiplier(1).build(),
                VirtualCell.builder().letter("A").lastPlayed(true).value(3).wordScoreMultiplier(1).build(),
                VirtualCell.builder().letter("R").lastPlayed(true).value(4).wordScoreMultiplier(1).build());

        final ConstructedWord constructedWord = ConstructedWord.builder().cells(cells).build();

        assertThat(scoreService.calculateConstructedWordScore(constructedWord), equalTo(20));
    }

    @Test
    void test_calculate_constructed_word_score_that_uses_empty_letter_multiplier_cell() {
        final List<VirtualCell> cells = Arrays.asList(
                VirtualCell.builder().letter("F").lastPlayed(true).value(1).letterValueMultiplier(2).build(),
                VirtualCell.builder().letter("E").lastPlayed(true).value(2).letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").lastPlayed(true).value(3).letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("R").lastPlayed(true).value(4).letterValueMultiplier(1).build());

        final ConstructedWord constructedWord = ConstructedWord.builder().cells(cells).build();

        assertThat(scoreService.calculateConstructedWordScore(constructedWord), equalTo(11));
    }

    @Test
    void test_calculate_constructed_word_score_that_uses_non_empty_word_multiplier_cell() {
        final List<VirtualCell> cells = Arrays.asList(
                VirtualCell.builder().letter("F").lastPlayed(false).value(1).wordScoreMultiplier(2).build(),
                VirtualCell.builder().letter("E").lastPlayed(true).value(2).wordScoreMultiplier(1).build(),
                VirtualCell.builder().letter("A").lastPlayed(true).value(3).wordScoreMultiplier(1).build(),
                VirtualCell.builder().letter("R").lastPlayed(true).value(4).wordScoreMultiplier(1).build());

        final ConstructedWord constructedWord = ConstructedWord.builder().cells(cells).build();

        assertThat(scoreService.calculateConstructedWordScore(constructedWord), equalTo(10));
    }

    @Test
    void test_calculate_constructed_word_score_that_uses_non_empty_letter_multiplier_cell() {
        final List<VirtualCell> cells = Arrays.asList(
                VirtualCell.builder().letter("F").lastPlayed(false).value(1).letterValueMultiplier(2).build(),
                VirtualCell.builder().letter("E").lastPlayed(true).value(2).letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").lastPlayed(true).value(3).letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("R").lastPlayed(true).value(4).letterValueMultiplier(1).build());

        final ConstructedWord constructedWord = ConstructedWord.builder().cells(cells).build();

        assertThat(scoreService.calculateConstructedWordScore(constructedWord), equalTo(10));
    }

    @Test
    void test_calculate_bingo_bonus_that_uses_single_word_and_all_tiles() {
        final List<VirtualCell> cells = Arrays.asList(
                VirtualCell.builder().letter("F").letterValueMultiplier(2).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("R").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("D").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("Y").letterValueMultiplier(1).build());

        final ConstructedWord constructedWord = ConstructedWord.builder().cells(cells).build();

        final List<VirtualTile> tiles = Arrays.asList(VirtualTile.builder().letter("F").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("R").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("D").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("Y").sealed(true).build());

        final VirtualRack rack = VirtualRack.builder().tiles(tiles).build();

        final List<Bonus> bonuses = scoreService.calculateBonuses(Arrays.asList(constructedWord), rack);

        assertThat(bonuses.size(), equalTo(1));
        assertThat(bonuses.get(0).getScore(), equalTo(50));
    }

    @Test
    void test_calculate_bingo_bonus_that_uses_single_word_and_all_tiles_but_not_full_rack() {
        final List<VirtualCell> cells = Arrays.asList(
                VirtualCell.builder().letter("F").letterValueMultiplier(2).build(),
                VirtualCell.builder().letter("E").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("R").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("E").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("D").letterValueMultiplier(1).build());

        final ConstructedWord constructedWord = ConstructedWord.builder().cells(cells).build();

        final List<VirtualTile> tiles = Arrays.asList(VirtualTile.builder().letter("F").sealed(true).build(),
                VirtualTile.builder().letter("E").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("R").sealed(true).build(),
                VirtualTile.builder().letter("E").sealed(true).build(),
                VirtualTile.builder().letter("D").sealed(true).build());

        final VirtualRack rack = VirtualRack.builder().tiles(tiles).build();

        assertThat(scoreService.calculateBonuses(Arrays.asList(constructedWord), rack), empty());
    }

    @Test
    void test_calculate_bingo_bonus_that_uses_single_word_but_not_all_tiles() {
        final List<VirtualCell> cells = Arrays.asList(
                VirtualCell.builder().letter("F").letterValueMultiplier(2).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("R").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("D").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("Y").letterValueMultiplier(1).build());

        final ConstructedWord constructedWord = ConstructedWord.builder().cells(cells).build();

        final List<VirtualTile> tiles = Arrays.asList(VirtualTile.builder().letter("F").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("R").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("D").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("Y").sealed(false).build());

        final VirtualRack rack = VirtualRack.builder().tiles(tiles).build();

        assertThat(scoreService.calculateBonuses(Arrays.asList(constructedWord), rack), empty());
    }

    @Test
    void test_calculate_bingo_bonus_that_uses_multiple_words_and_all_tiles() {
        final List<VirtualCell> fearCells = Arrays.asList(
                VirtualCell.builder().letter("F").letterValueMultiplier(2).build(),
                VirtualCell.builder().letter("E").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("R").letterValueMultiplier(1).build());

        final ConstructedWord fearWord = ConstructedWord.builder().cells(fearCells).build();

        final List<VirtualCell> dayCells = Arrays.asList(
                VirtualCell.builder().letter("D").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("A").letterValueMultiplier(1).build(),
                VirtualCell.builder().letter("Y").letterValueMultiplier(1).build());

        final ConstructedWord dayWord = ConstructedWord.builder().cells(dayCells).build();

        final List<VirtualTile> tiles = Arrays.asList(VirtualTile.builder().letter("F").sealed(true).build(),
                VirtualTile.builder().letter("E").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("R").sealed(true).build(),
                VirtualTile.builder().letter("D").sealed(true).build(),
                VirtualTile.builder().letter("A").sealed(true).build(),
                VirtualTile.builder().letter("Y").sealed(true).build());

        final VirtualRack rack = VirtualRack.builder().tiles(tiles).build();

        assertThat(scoreService.calculateBonuses(Arrays.asList(fearWord, dayWord), rack), empty());
    }

}
