package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.CellDao;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Cell;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static com.gamecity.scrabble.Constants.Game.BOARD_SIZE;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_EXISTING_LETTERS;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_EXISTING_WORDS;
import static com.gamecity.scrabble.model.BoardScanFlag.SCAN_EXISTING_WORDS;
import static com.gamecity.scrabble.model.BoardScanFlag.SCAN_SINGLE_LETTERS;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_NEW_LETTERS;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_NEW_WORDS;

class TestVirtualBoardService extends AbstractServiceTest {

    @Mock
    private CellDao cellDao;

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private VirtualBoardService virtualBoardService = new VirtualBoardServiceImpl(cellDao, redisRepository);

    @Test
    void test_create_board() {
        final List<Cell> cells = new ArrayList<>(BOARD_SIZE * BOARD_SIZE);
        IntStream.range(1, BOARD_SIZE + 1).forEach(rowNumber -> {
            IntStream.range(1, BOARD_SIZE + 1).forEach(columnNumber -> {
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
        final List<Cell> cells = new ArrayList<>(BOARD_SIZE * BOARD_SIZE);

        when(cellDao.list()).thenReturn(cells);

        try {
            virtualBoardService.createBoard(DEFAULT_GAME_ID);

            fail("The board is created with insufficient cells");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), equalTo("Cell [{1},{1}] is not found!"));
        }
    }

    @Test
    void test_scan_word_detection_ends_in_right() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 8, "WEAK");

        // the extension ENIN is linked to the WEAK word
        locateNewHorizontalWord(8, 12, "ENIN");

        // the extension G is added to the next cell that is in the next row
        locateNewHorizontalWord(9, 1, "G");

        try {
            virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                    Set.of(LOG_EXISTING_LETTERS, LOG_NEW_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

            fail("Word detection didn't end in right");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode()));
        }
    }

    @Test
    void test_scan_word_detection_ends_in_bottom() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 8, "WEAK");

        // the extension ENIN is linked to the WEAK word
        locateNewHorizontalWord(12, 8, "ENIN");

        // the extension G is added to the next cell that is in the next column
        locateNewHorizontalWord(1, 9, "G");

        try {
            virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                    Set.of(LOG_EXISTING_LETTERS, LOG_NEW_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

            fail("Word detection didn't end in bottom");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode()));
        }
    }

    @Test
    void test_scan_new_two_words_not_linked_to_existing_words() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 7, "WEAK");

        // the word HEAL not linked to an existing word
        locateNewHorizontalWord(3, 7, "HEAL");

        // the word HOST not linked to an existing word, but linked to HEAL word
        locateNewVerticalWord(4, 7, "OST");

        try {
            virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                    Set.of(LOG_EXISTING_LETTERS, LOG_NEW_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

            fail("Unlinked word is played");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.WORDS_ARE_NOT_LINKED.getCode()));
        }
    }

    @Test
    void test_scan_two_words_linked_to_existing_words() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 7, "WEAK");

        // the word SNO(W) is directly linked to the existing WEAK word
        locateNewVerticalWord(5, 7, "SNO");

        // the word BULL(S) is linked to the new RAW word
        locateNewHorizontalWord(5, 3, "BULL");

        final List<ConstructedWord> newWords = virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                Set.of(LOG_EXISTING_LETTERS, LOG_NEW_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

        final List<String> words = newWords.stream()
                .map(newWord -> newWord.getBuilder().toString())
                .collect(Collectors.toList());

        assertThat(words.contains("SNOW"), equalTo(true));
        assertThat(words.contains("BULLS"), equalTo(true));
    }

    @Test
    void test_scan_new_word_extends_an_existing_word() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 7, "WEAK");

        // the word ENING will extend the existing WEAK word
        locateNewHorizontalWord(8, 11, "ENING");

        final List<ConstructedWord> newWords = virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                Set.of(LOG_EXISTING_LETTERS, LOG_NEW_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

        final List<String> words = newWords.stream()
                .map(newWord -> newWord.getBuilder().toString())
                .collect(Collectors.toList());

        assertThat(words.contains("WEAKENING"), equalTo(true));
    }

    @Test
    void test_scan_use_a_letter_from_an_extended_word() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 7, "WEAK");

        // the word (WEAK)ENING will extend the existing WEAK word
        locateNewHorizontalWord(8, 11, "ENING");

        // the word (G)OAL will be linked to the WEAKENING word
        locateNewVerticalWord(9, 15, "OAL");

        final List<ConstructedWord> newWords = virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                Set.of(LOG_EXISTING_LETTERS, LOG_NEW_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

        final List<String> words = newWords.stream()
                .map(newWord -> newWord.getBuilder().toString())
                .collect(Collectors.toList());

        assertThat(words.contains("WEAKENING"), equalTo(true));
        assertThat(words.contains("GOAL"), equalTo(true));
    }

    @Test
    void test_scan_the_same_word_more_than_once() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        createExistingHorizontalWord(8, 7, "WEAK");

        // the word (W)EAK will be linked to the W letter
        locateNewVerticalWord(9, 7, "EAK");

        final List<ConstructedWord> newWords = virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                Set.of(SCAN_EXISTING_WORDS, LOG_EXISTING_LETTERS, LOG_NEW_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

        final List<String> words = newWords.stream()
                .map(newWord -> newWord.getBuilder().toString())
                .collect(Collectors.toList());

        assertThat(words.stream().filter(word -> word.equals("WEAK")).count(), equalTo(2L));
    }

    @Test
    void test_scan_single_letter_not_allowed() {
        createBoardMatrix();

        // the letter A is not linked to anywhere
        locateNewHorizontalWord(8, 8, "A");

        try {
            virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                    Set.of(SCAN_EXISTING_WORDS, LOG_EXISTING_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

            fail("Single letter words are not detected");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode()));
        }
    }

    @Test
    void test_scan_single_letter_with_valid_word_not_allowed() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        locateNewHorizontalWord(8, 8, "WEAK");

        // the letter A is not linked to anywhere
        locateNewHorizontalWord(1, 1, "A");

        try {
            virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(),
                    Set.of(LOG_EXISTING_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

            fail("Single letter words are not detected");
        } catch (GameException e) {
            assertThat(e.getCode(), equalTo(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED.getCode()));
        }
    }

    @Test
    void test_scan_single_letter_allowed() {
        createBoardMatrix();

        // the word WEAK is an existing word in the board
        locateNewHorizontalWord(8, 8, "WEAK");

        final List<ConstructedWord> newWords = virtualBoardService.scanWords(DEFAULT_GAME_ID, createVirtualBoard(), Set
                .of(SCAN_SINGLE_LETTERS, SCAN_EXISTING_WORDS, LOG_EXISTING_LETTERS, LOG_EXISTING_WORDS, LOG_NEW_WORDS));

        final List<String> words = newWords.stream()
                .map(newWord -> newWord.getBuilder().toString())
                .collect(Collectors.toList());

        assertThat(words.contains("W"), equalTo(true));
        assertThat(words.contains("E"), equalTo(true));
        assertThat(words.contains("A"), equalTo(true));
        assertThat(words.contains("K"), equalTo(true));
        assertThat(words.contains("WEAK"), equalTo(true));
    }

    private VirtualBoard createVirtualBoard() {
        return new VirtualBoard(Arrays.stream(boardMatrix).flatMap(Arrays::stream).collect(Collectors.toList()));
    }

    private void locateNewHorizontalWord(int startingRow, int startingColumn, String word) {
        int columnNumber = startingColumn;

        for (char letter : word.toCharArray()) {
            final VirtualCell virtualCell = boardMatrix[startingRow - 1][columnNumber - 1];
            virtualCell.setLastPlayed(false);
            virtualCell.setLetter(String.valueOf(letter).toUpperCase());
            virtualCell.setSealed(false);

            columnNumber = columnNumber + 1;
        }
    }

    private void locateNewVerticalWord(int startingRow, int startingColumn, String word) {
        int rowNumber = startingRow;

        for (char letter : word.toCharArray()) {
            final VirtualCell virtualCell = boardMatrix[rowNumber - 1][startingColumn - 1];
            virtualCell.setLastPlayed(false);
            virtualCell.setLetter(String.valueOf(letter).toUpperCase());
            virtualCell.setSealed(false);

            rowNumber = rowNumber + 1;
        }
    }

}
