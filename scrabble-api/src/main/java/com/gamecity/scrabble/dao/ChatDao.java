package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;

/**
 * Provides dao operations for {@link Chat} entity
 * 
 * @author ekarakus
 */
public interface ChatDao extends BaseDao<Chat> {

    /**
     * Gets the {@link List list} of the {@link Chat chats} in the {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the chat messages
     */
    List<Chat> getChats(Long gameId);

}
