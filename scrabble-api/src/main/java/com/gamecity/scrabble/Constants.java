package com.gamecity.scrabble;

/**
 * Constant variables used by the application
 * 
 * @author ekarakus
 */
public interface Constants {

    /**
     * Game properties
     */
    interface Game {
        /**
         * Number of tiles in a rack
         */
        Integer RACK_SIZE = 7;

        /**
         * Number of rows in a board
         */
        Integer BOARD_ROW_SIZE = 15;

        /**
         * Number of columns in a board
         */
        Integer BOARD_COLUMN_SIZE = 15;

        /**
         * Bonus score for using all tiles (7) in the rack
         */
        Integer BONUS_SCORE = 50;
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
        String getChatCount = "getChatCount";
        String getChats = "getChats";

        // Game
        String searchByUser = "searchByUser";
        String searchGames = "searchGames";

        // Player
        String getPlayersByUserId = "getPlayersByUserId";
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
