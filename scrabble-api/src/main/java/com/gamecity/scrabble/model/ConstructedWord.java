package com.gamecity.scrabble.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Details of a word that is created on the board
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConstructedWord {

    // cells used by the letters
    @Builder.Default
    private List<VirtualCell> cells = new ArrayList<>();

    // char sequence created by the letters
    @Builder.Default
    private StringBuilder builder = new StringBuilder();

    // direction of the word
    private Direction direction;

    // whether the word is linked to existing words
    private boolean linked;

    // the word stored in the dictionary
    private DictionaryWord dictionaryWord;

    // calculated score of the word
    private Integer score;

    /**
     * Reset the constructed word
     */
    public void reset() {
        cells = new ArrayList<>();
        builder = new StringBuilder();
        linked = false;
        dictionaryWord = null;
        score = null;
    }

    /**
     * Whether the constructed word is an existing word
     * 
     * @return true if is existing word
     */
    public boolean isExistingWord() {
        return this.getCells().stream().allMatch(VirtualCell::isSealed);
    }

}