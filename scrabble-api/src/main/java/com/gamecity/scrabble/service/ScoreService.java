package com.gamecity.scrabble.service;

import java.util.List;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.Bonus;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.VirtualRack;

/**
 * Provides services for score calculations after each round
 * 
 * @author ekarakus
 */
public interface ScoreService {

    /**
     * Calculates the <code>score</code> of a {@link ConstructedWord word}
     * 
     * @param constructedWord word constructed by the {@link Player player}
     * @return the word score
     */
    Integer calculateConstructedWordScore(ConstructedWord constructedWord);

    /**
     * Gets the {@link List list} of rewarded {@link Bonus bonuses} after the played round
     * 
     * @param constructedWords words created in this round of the {@link Game game}
     * @param virtualRack      rack of the {@link Player player}
     * @return the list of {@link Bonus bonuses}
     */
    List<Bonus> calculateBonuses(List<ConstructedWord> constructedWords, VirtualRack virtualRack);

}
