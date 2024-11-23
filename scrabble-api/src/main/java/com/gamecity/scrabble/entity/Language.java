package com.gamecity.scrabble.entity;

/**
 * Language of the {@link Tile tiles} in a {@link Bag bag}
 * 
 * @author ekarakus
 */
public enum Language {

    /**
     * English
     */
    en(26),

    /**
     * Turkish
     */
    tr(29),

    /**
     * French
     */
    fr(26),

    /**
     * Dutch
     */
    nl(26),

    /**
     * German
     */
    de(26);

    private int alphabetSize;

    Language(int alphabetSize) {
        this.alphabetSize = alphabetSize;
    }

    /**
     * Returns the number of letters in the alphabet of the selected language
     * 
     * @return the alphabet size
     */
    public int getAlphabetSize() {
        return alphabetSize;
    }

}
