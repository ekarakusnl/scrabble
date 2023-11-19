package com.gamecity.scrabble.service.helper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for game validations
 */
@Slf4j
public class GameValidationHelper {

    /**
     * Whether the playing player has the turn
     * 
     * @param playerNumber        <code>number</code> of the player
     * @param currentPlayerNumber <code>number</code> of the current player
     */
    public static void hasCurrentPlayerTurn(final Integer playerNumber, final Integer currentPlayerNumber) {
        if (!playerNumber.equals(currentPlayerNumber)) {
            throw new GameException(GameError.TURN_OF_ANOTHER_PLAYER);
        }
    }

    /**
     * Whether the game has non empty center cell
     * 
     * @param boardMatrix {@link VirtualBoard Board} matrix represent the board in two-dimensions
     */
    public static void hasNonEmptyCenter(VirtualCell[][] boardMatrix) {
        final boolean isCenterUsed = Arrays.stream(boardMatrix)
                .flatMap(Arrays::stream)
                .anyMatch(cell -> cell.isCenter() && cell.getLetter() != null);
        if (!isCenterUsed) {
            throw new GameException(GameError.CENTER_CANNOT_BE_EMPTY);
        }
    }

    /**
     * Whether the words are defined in the dictionary
     * 
     * @param constructedWords {@link List} of constructed words
     * @param language         {@link Language} of the constructed words
     */
    public static void hasWordsInDictionary(final List<ConstructedWord> constructedWords, final Language language) {
        final List<ConstructedWord> invalidWords = constructedWords.stream()
                .filter(word -> word.getDictionaryWord() == null)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(invalidWords)) {
            final String commaSeperatedInvalidWords = String.join(",",
                    invalidWords.stream().map(ConstructedWord::getWordBuilder).collect(Collectors.toList()));
            log.debug("Word(s) {} are not found in {} dictionary", commaSeperatedInvalidWords, language);
            throw new GameException(GameError.WORDS_ARE_NOT_FOUND, Arrays.asList(commaSeperatedInvalidWords, language.name()));
        }
    }

    /**
     * Whether the constructed words are linked to existing words on the board
     * 
     * @param constructedWords {@link List} of constructed words
     * @param boardMatrix      {@link VirtualBoard Board} matrix represent the board in two-dimensions
     */
    public static void hasValidBoardLinks(final List<ConstructedWord> constructedWords, final VirtualCell[][] boardMatrix) {
        final List<ConstructedWord> unlinkedWords = constructedWords.stream().filter(word -> !word.isLinked()).collect(Collectors.toList());

        if (unlinkedWords.isEmpty()) {
            return;
        }

        int unlinkedWordCount = unlinkedWords.size();
        while (unlinkedWordCount > 0) {
            final List<ConstructedWord> updatedUnlinkedWords = unlinkedWords.stream().map(word -> {
                return linkWord(word, boardMatrix);
            }).filter(word -> !word.isLinked()).collect(Collectors.toList());

            if (updatedUnlinkedWords.isEmpty()) {
                // all words are linked
                return;
            } else if (updatedUnlinkedWords.size() < unlinkedWordCount) {
                // some words are linked, update the unlinked word count then try to link the remaining words
                unlinkedWordCount = updatedUnlinkedWords.size();
            } else {
                // since unlinked words count didn't change this means that trying one more time doesn't make any
                // difference
                final String commaSeperatedUnlinkedWords = String.join(",",
                        unlinkedWords.stream().map(ConstructedWord::getWordBuilder).collect(Collectors.toList()));
                throw new GameException(GameError.WORDS_ARE_NOT_LINKED, Arrays.asList(commaSeperatedUnlinkedWords));
            }
        }
    }

    /*
     * Link the new words to the existing words
     */
    private static ConstructedWord linkWord(final ConstructedWord word, final VirtualCell[][] boardMatrix) {
        word.getCells().forEach(cell -> {
            if (word.isLinked()) {
                return;
            }

            final Integer rowIndex = cell.getRowNumber() - 1;
            final Integer columnIndex = cell.getColumnNumber() - 1;

            if ((cell.isSealed() || (cell.isHasRight() && boardMatrix[rowIndex][columnIndex + 1].isSealed()))
                    || (cell.isHasLeft() && boardMatrix[rowIndex][columnIndex - 1].isSealed())
                    || (cell.isHasTop() && boardMatrix[rowIndex - 1][columnIndex].isSealed())
                    || (cell.isHasBottom() && boardMatrix[rowIndex + 1][columnIndex].isSealed())) {
                word.setLinked(true);

                // seal the cells if the word is a linked word
                word.getCells().forEach(c -> c.setSealed(true));
            }
        });
        return word;
    }

    /**
     * Whether the word has no single letter words
     * 
     * @param virtualBoard {@link VirtualBoard Board} to check the words
     */
    public static void hasNoSingleLetterWords(final VirtualBoard virtualBoard) {
        // detect single word letter
        final List<String> singleLetterWords = virtualBoard.getCells()
                .stream()
                .filter(cell -> cell.getLetter() != null && !cell.isSealed())
                .map(VirtualCell::getLetter)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(singleLetterWords)) {
            final String commaSeperatedSingleLetterWords = String.join(",", singleLetterWords);
            log.debug("Single letter word(s) {} are detected", commaSeperatedSingleLetterWords);
            throw new GameException(GameError.SINGLE_LETTER_WORDS_NOT_ALLOWED, Arrays.asList(commaSeperatedSingleLetterWords));
        }
    }

}
