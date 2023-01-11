package com.gamecity.scrabble.dao;

import java.util.List;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Word;

/**
 * Provides dao operations for {@link Word} entity
 * 
 * @author ekarakus
 */
public interface WordDao extends BaseDao<Word> {

    /**
     * Gets the {@link List list} of {@link Word words} played in a {@link Game game}
     * 
     * @param gameId <code>id</code> of the game
     * @return the list of words
     */
    List<Word> getWords(Long gameId);

}
