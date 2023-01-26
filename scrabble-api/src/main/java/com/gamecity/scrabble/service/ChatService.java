package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Chat;
import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;

/**
 * Provides services for {@link Chat chats} between {@link Player players }in a {@link Game}
 * 
 * @author ekarakus
 */
public interface ChatService extends BaseService<Chat> {

    /**
     * Gets the {@link List list} of {@link Chat chats} in the {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the list of chats
     */
    List<Chat> getChats(Long gameId);

}
