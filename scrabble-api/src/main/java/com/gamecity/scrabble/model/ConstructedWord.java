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
    private StringBuilder wordBuilder = new StringBuilder();

    // direction of the word
    private Direction direction;

    // whether the word is linked to existing words
    private boolean linked;

    // the word stored in the dictionary
    private DictionaryWord dictionaryWord;

    // calculated score of the word
    private Integer score;

    /**
     * 
     */
    public enum Direction {
        /**
         * Vertical word
         */
        VERTICAL,

        /**
         * Horizontal word
         */
        HORIZONTAL
    }

    /**
     * Reset the properties of the word
     */
    public void reset() {
        cells = new ArrayList<>();
        wordBuilder = new StringBuilder();
        linked = false;
        direction = null;
        dictionaryWord = null;
        score = null;
    }

}