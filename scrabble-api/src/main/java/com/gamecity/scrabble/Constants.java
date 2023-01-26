package com.gamecity.scrabble;

/**
 * Constant variables used by the application
 * 
 * @author ekarakus
 */
public interface Constants {

    /**
     * Cache keys used by Redis
     */
    interface CacheKey {
        String ACTION = "ACTION";
        String PLAYERS = "PLAYERS";
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

        // Cell
        String getCells = "getCells";

        // Chat
        String getChatCount = "getChatCount";
        String getChats = "getChats";

        // Game
        String getLastGames = "getLastGames";
        String getByUser = "getByUser";

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
