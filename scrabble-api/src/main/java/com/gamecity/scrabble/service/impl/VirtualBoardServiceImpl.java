package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.dao.CellDao;
import com.gamecity.scrabble.dao.RedisRepository;
import com.gamecity.scrabble.entity.Cell;
import com.gamecity.scrabble.model.BoardScanFlag;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.Direction;
import com.gamecity.scrabble.model.ExtensionPoint;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.service.VirtualBoardService;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import lombok.extern.slf4j.Slf4j;

import static com.gamecity.scrabble.Constants.Game.BOARD_SIZE;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_EXISTING_LETTERS;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_EXISTING_WORDS;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_NEW_LETTERS;
import static com.gamecity.scrabble.model.BoardScanFlag.LOG_NEW_WORDS;
import static com.gamecity.scrabble.model.BoardScanFlag.SCAN_EXISTING_WORDS;
import static com.gamecity.scrabble.model.BoardScanFlag.SCAN_SINGLE_LETTERS;
import static com.gamecity.scrabble.model.Direction.HORIZONTAL;
import static com.gamecity.scrabble.model.Direction.VERTICAL;

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
        final VirtualCell[] virtualCells = new VirtualCell[BOARD_SIZE * BOARD_SIZE];

        final Map<Integer, Cell> cells = cellDao.list()
                .stream()
                .collect(Collectors.toMap(Cell::getCellNumber, cell -> cell));

        IntStream.range(1, BOARD_SIZE + 1).forEach(rowNumber -> {
            IntStream.range(1, BOARD_SIZE + 1).forEach(columnNumber -> {
                final Cell cell = cells.get((rowNumber - 1) * BOARD_SIZE + columnNumber);
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
        final VirtualBoard virtualBoard = redisRepository.getBoard(gameId, version);
        return new VirtualBoard(virtualBoard.getCells());
    }

    /*
     * Finds existing words on the board
     */
    @Override
    public List<ConstructedWord> scanWords(Long gameId, VirtualBoard virtualBoard, Set<BoardScanFlag> boardScanFlags) {
        final List<ConstructedWord> words = new ArrayList<>();

        // find horizontal and vertical words
        Arrays.stream(Direction.values()).forEach(direction -> {

            IntStream.range(1, BOARD_SIZE + 1).forEach(index -> {
                final ConstructedWord constructedWord = ConstructedWord.builder().direction(direction).build();

                IntStream.range(1, BOARD_SIZE + 1).forEach(oppositeIndex -> {
                    final VirtualCell cell = Direction.HORIZONTAL == direction
                            ? virtualBoard.getCell(index, oppositeIndex)
                            : virtualBoard.getCell(oppositeIndex, index);

                    final ConstructedWord detectedWord = scanWordsByDirection(gameId, cell, boardScanFlags,
                            constructedWord);
                    if (detectedWord != null) {
                        words.add(detectedWord);
                    }
                });
            });
        });

        if (!boardScanFlags.contains(SCAN_SINGLE_LETTERS)) {
            hasNoSingleLetterWords(virtualBoard);
        }
        scanBoardLinks(words, virtualBoard);

        return words;
    }

    /*
     * Finds words on the board by direction
     */
    private ConstructedWord scanWordsByDirection(Long gameId, VirtualCell virtualCell,
                                                 Set<BoardScanFlag> boardScanFlags, ConstructedWord constructedWord) {
        if (virtualCell.getLetter() != null) {
            constructedWord.getBuilder().append(virtualCell.getLetter());
            constructedWord.getCells().add(virtualCell);
            constructedWord.setLinked(constructedWord.isLinked() || virtualCell.isCenter());

            final boolean isNewLetter = !virtualCell.isSealed();

            // letter is placed this round
            if (isNewLetter) {
                virtualCell.setLastPlayed(true);
            }

            // log the letter
            if ((boardScanFlags.contains(LOG_NEW_LETTERS) && isNewLetter)
                    || (boardScanFlags.contains(LOG_EXISTING_LETTERS) && !isNewLetter)) {
                log.debug("{} letter '{}' is spotted on [{},{}] on game {}", constructedWord.getDirection(),
                        virtualCell.getLetter(), virtualCell.getRowNumber(), virtualCell.getColumnNumber(), gameId);
            }
        }

        boolean emptyCell = virtualCell.getLetter() == null;
        boolean horizontalLast = HORIZONTAL == constructedWord.getDirection()
                && virtualCell.getColumnNumber() == BOARD_SIZE;
        boolean verticalLast = VERTICAL == constructedWord.getDirection() && virtualCell.getRowNumber() == BOARD_SIZE;

        // if the cell is neither in the last row/column nor empty, then keep detecting the word
        if (!emptyCell && !horizontalLast && !verticalLast) {
            return null;
        }

        // cannot create a word without a letter
        if (constructedWord.getBuilder().length() == 0) {
            constructedWord.reset();
            return null;
        }

        // can include a single letter word only if the feature is enabled
        if (constructedWord.getBuilder().length() == 1 && !boardScanFlags.contains(SCAN_SINGLE_LETTERS)) {
            constructedWord.reset();
            return null;
        }

        final boolean isExistingWord = constructedWord.isExistingWord();

        // can include an existing word only if the feature is enabled
        if (isExistingWord && !boardScanFlags.contains(SCAN_EXISTING_WORDS)) {
            constructedWord.reset();
            return null;
        }

        // seal the cells if the word is a linked word
        if (constructedWord.isLinked()) {
            constructedWord.getCells().forEach(c -> c.setSealed(true));
        }

        // found a word
        final ConstructedWord detectedWord = ConstructedWord.builder()
                .cells(constructedWord.getCells())
                .builder(constructedWord.getBuilder())
                .direction(constructedWord.getDirection())
                .linked(constructedWord.isLinked())
                .build();

        constructedWord.reset();

        // log the word
        // TODO add a test for enabled flags
        if ((boardScanFlags.contains(LOG_EXISTING_WORDS) && isExistingWord)
                || (boardScanFlags.contains(LOG_NEW_WORDS) && !isExistingWord)) {
            log.debug("{} word '{}' has been detected on game {}", detectedWord.getDirection().name(),
                    detectedWord.getBuilder(), gameId);
        }

        return detectedWord;
    }

    /**
     * Validate that the board has no single letter words if SCAN_SINGLE_LETTERS is disabled
     */
    private static void hasNoSingleLetterWords(final VirtualBoard virtualBoard) {
        // detect single word letters
        final List<String> singleLetterWords = virtualBoard.getCells()
                .stream()
                .filter(virtualCell -> isSingleCell(virtualBoard, virtualCell))
                .map(VirtualCell::getLetter)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(singleLetterWords)) {
            final String commaSeperatedSingleLetterWords = String.join(",", singleLetterWords);
            log.debug("Single letter word(s) {} are detected", commaSeperatedSingleLetterWords);
            throw new GameException(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED,
                    Arrays.asList(commaSeperatedSingleLetterWords));
        }
    }

    private static boolean isSingleCell(final VirtualBoard virtualBoard, final VirtualCell virtualCell) {
        return virtualCell.getLetter() != null && !virtualCell.isSealed()
                && virtualBoard.hasNotUsedNeighbour(virtualCell, HORIZONTAL, ExtensionPoint.FORWARD)
                && virtualBoard.hasNotUsedNeighbour(virtualCell, HORIZONTAL, ExtensionPoint.BACKWARD)
                && virtualBoard.hasNotUsedNeighbour(virtualCell, VERTICAL, ExtensionPoint.FORWARD)
                && virtualBoard.hasNotUsedNeighbour(virtualCell, VERTICAL, ExtensionPoint.BACKWARD);
    }

    /*
     * Scans links between new words and existing words
     */
    private static void scanBoardLinks(final List<ConstructedWord> constructedWords, final VirtualBoard virtualBoard) {
        final List<ConstructedWord> unlinkedWords = constructedWords.stream()
                .filter(word -> !word.isLinked())
                .collect(Collectors.toList());

        // no unlinked words
        if (unlinkedWords.isEmpty()) {
            return;
        }

        int unlinkedWordCount = unlinkedWords.size();
        while (unlinkedWordCount > 0) {
            final List<ConstructedWord> updatedUnlinkedWords = unlinkedWords.stream().map(word -> {
                return linkWord(word, virtualBoard);
            }).filter(word -> !word.isLinked()).collect(Collectors.toList());

            if (updatedUnlinkedWords.isEmpty()) {
                // all words are linked
                unlinkedWordCount = 0;
            } else if (updatedUnlinkedWords.size() < unlinkedWordCount) {
                // some words are linked, update the unlinked word count then try to link the remaining words
                unlinkedWordCount = updatedUnlinkedWords.size();
            } else {
                // since unlinked words count didn't change this means that trying one more time
                // wouldn't make any difference
                final String commaSeperatedUnlinkedWords = String.join(",",
                        unlinkedWords.stream().map(ConstructedWord::getBuilder).collect(Collectors.toList()));
                throw new GameException(GameError.WORDS_ARE_NOT_LINKED, Arrays.asList(commaSeperatedUnlinkedWords));
            }
        }
    }

    /*
     * Link new words to existing words
     */
    private static ConstructedWord linkWord(final ConstructedWord word, final VirtualBoard virtualBoard) {
        word.getCells().forEach(cell -> {
            if (word.isLinked()) {
                return;
            }

            // TODO add tests
            if ((cell.isSealed()
                    || (virtualBoard.hasSealedNeighbour(cell, Direction.HORIZONTAL, ExtensionPoint.FORWARD)))
                    || (virtualBoard.hasSealedNeighbour(cell, Direction.HORIZONTAL, ExtensionPoint.BACKWARD))
                    || (virtualBoard.hasSealedNeighbour(cell, Direction.VERTICAL, ExtensionPoint.FORWARD))
                    || (virtualBoard.hasSealedNeighbour(cell, Direction.VERTICAL, ExtensionPoint.BACKWARD))) {
                word.setLinked(true);

                // seal the cells if the word is a linked word
                word.getCells().forEach(c -> c.setSealed(true));
            }
        });
        return word;
    }

}
