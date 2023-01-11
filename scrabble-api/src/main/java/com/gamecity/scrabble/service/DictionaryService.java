package com.gamecity.scrabble.service;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Word;
import com.gamecity.scrabble.model.Dictionary;

/**
 * Provides services for {@link Dictionary dictionaries} for the played {@link Word words} in a
 * {@link Game game}
 * 
 * @author ekarakus
 */
public interface DictionaryService {

    /**
     * Whether the selected {@link Dictionary dictionary} has the word
     * 
     * @param word     <code>word</code> to search in the dictionary
     * @param language <code>language</code> of the dictionary
     * @return true if the words exists
     */
    boolean hasWord(String word, Language language);

}
