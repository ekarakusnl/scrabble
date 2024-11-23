package com.gamecity.scrabble;

/**
 * Constant variables used by the application
 * 
 * @author ekarakus
 */
public interface Constants {

    /**
     * Game constants
     */
    interface Game {
        /**
         * Number of tiles in a rack
         */
        Integer RACK_SIZE = 7;

        /**
         * Number of rows/columns in a board
         */
        Integer BOARD_SIZE = 15;

        /**
         * No score
         */
        Integer NO_SCORE = 0;

        /**
         * Bingo bonus for using all tiles (7) in the rack
         */
        Integer BINGO_SCORE = 50;

        /**
         * Number of skipped rounds to end the game automatically
         */
        Integer MAXIMUM_SKIPPED_ROUNDS_IN_A_ROW = 2;

        /**
         * Number of minutes until the game is terminated if it doesn't start
         */
        Integer TERMINATE_GAME_DURATION_MINUTES = 10;

        /**
         * Minimum word length
         */
        Integer MIN_WORD_LENGTH = 2;
    }

    /**
     * Cache keys used by Redis
     */
    interface CacheKey {
        String ACTION = "ACTION";
        String CHATS = "CHATS";
        String BOARD = "BOARD";
        String RACK = "RACK";
        String TILES = "TILES";
    }

    /**
     * Named queries used by Hibernate
     */
    interface NamedQuery {
        // Action
        String getActions = "getActions";
        String getLastAction = "getLastAction";
        String getActionByVersion = "getActionByVersion";
        String getLastActionsByCount = "getLastActionsByCount";

        // Cell
        String getCells = "getCells";

        // Chat
        String getChats = "getChats";

        // Game
        String searchByUser = "searchByUser";
        String searchGames = "searchGames";

        // Player
        String getPlayerByUserId = "getPlayerByUserId";
        String getPlayerByPlayerNumber = "getPlayerByPlayerNumber";
        String getCurrentPlayers = "getCurrentPlayers";

        // Tile
        String getTiles = "getTiles";

        // User
        String getUserByUsername = "getUserByUsername";
        String getUserByEmail = "getUserByEmail";

        // UserRole
        String getRolesByUserId = "getRolesByUserId";

        // Word
        String getWords = "getWords";
    }

}
