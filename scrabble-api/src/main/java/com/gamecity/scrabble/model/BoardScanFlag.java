package com.gamecity.scrabble.model;

/**
 * Feature flags for board scan service
 */
public enum BoardScanFlag {

    /**
     * Scan single letters
     */
    SCAN_SINGLE_LETTERS,

    /**
     * Scan existing words
     */
    SCAN_EXISTING_WORDS,

    /**
     * Log existing letters
     */
    LOG_EXISTING_LETTERS,

    /**
     * Log new letters
     */
    LOG_NEW_LETTERS,

    /**
     * Log existing words
     */
    LOG_EXISTING_WORDS,

    /**
     * Log new words
     */
    LOG_NEW_WORDS;

}
