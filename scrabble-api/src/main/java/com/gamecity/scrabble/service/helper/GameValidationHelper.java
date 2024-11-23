package com.gamecity.scrabble.service.helper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.VirtualBoard;
import com.gamecity.scrabble.service.exception.GameException;
import com.gamecity.scrabble.service.exception.error.GameError;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for game validations
 */
@Slf4j
public class GameValidationHelper {

    private GameValidationHelper() {
        // do not create a new instance
    }

    /**
     * Whether the playing player has the turn
     * 
     * @param playerNumber        <code>number</code> of the player
     * @param currentPlayerNumber <code>number</code> of the current player
     */
    public static void hasCurrentTurn(Integer playerNumber, Integer currentPlayerNumber) {
        if (!playerNumber.equals(currentPlayerNumber)) {
            throw new GameException(GameError.TURN_OF_ANOTHER_PLAYER);
        }
    }

    /**
     * Whether the game has non empty center cell
     * 
     * @param virtualBoard current board of the game
     */
    public static void hasNonEmptyCenter(VirtualBoard virtualBoard) {
        final boolean isCenterUsed = Arrays.stream(virtualBoard.getMatrix())
                .flatMap(Arrays::stream)
                .anyMatch(cell -> cell.isCenter() && cell.getLetter() != null);
        if (!isCenterUsed) {
            throw new GameException(GameError.CENTER_CANNOT_BE_EMPTY);
        }
    }

    /**
     * Whether the words are defined in the dictionary
     * 
     * @param constructedWords list of constructed words
     * @param language         language of the constructed words
     */
    public static void hasWordsInDictionary(List<ConstructedWord> constructedWords, Language language) {
        final List<ConstructedWord> invalidWords = constructedWords.stream()
                .filter(word -> word.getDictionaryWord() == null)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(invalidWords)) {
            final String commaSeperatedInvalidWords = String.join(",",
                    invalidWords.stream().map(ConstructedWord::getBuilder).collect(Collectors.toList()));
            log.debug("Word(s) {} are not found in {} dictionary", commaSeperatedInvalidWords, language);
            throw new GameException(GameError.WORDS_ARE_NOT_FOUND,
                    Arrays.asList(commaSeperatedInvalidWords, language.name()));
        }
    }

}
