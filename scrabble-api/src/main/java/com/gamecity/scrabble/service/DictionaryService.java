package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.model.DictionaryWord;

/**
 * Provides services for {@link Dictionary dictionaries} for the played {@link Word words} in a
 * {@link Game game}
 * 
 * @author ekarakus
 */
public interface DictionaryService {

    /**
     * Gets the selected word given in the specified {@link Language} from the dictionary
     * 
     * @param word     <code>word</code> to search in the dictionary
     * @param language <code>language</code> of the dictionary
     * @return true if the words exists
     */
    DictionaryWord get(String word, Language language);

    /**
     * Whether the word given in the specified {@link Language} exists in dictionary
     * 
     * @param word     <code>word</code> to search in the dictionary
     * @param language <code>language</code> of the dictionary
     * @return true if the words exists
     */
    boolean find(String word, Language language);

}
