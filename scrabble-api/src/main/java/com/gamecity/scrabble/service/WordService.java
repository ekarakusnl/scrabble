package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Word;

/**
 * Provides services for {@link Word words} played in a {@link Game game}
 * 
 * @author ekarakus
 */
public interface WordService extends BaseService<Word> {

    /**
     * Gets the {@link List list} of {@link Word words} played in a {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return list of the words
     */
    List<Word> getWords(Long gameId);

}
