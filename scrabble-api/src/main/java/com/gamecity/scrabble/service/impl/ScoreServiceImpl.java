package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.model.Bonus;
import com.gamecity.scrabble.model.ConstructedWord;
import com.gamecity.scrabble.model.VirtualCell;
import com.gamecity.scrabble.model.VirtualRack;
import com.gamecity.scrabble.model.VirtualTile;
import com.gamecity.scrabble.service.ScoreService;

import lombok.extern.slf4j.Slf4j;

import static com.gamecity.scrabble.Constants.Game.BINGO_SCORE;
import static com.gamecity.scrabble.Constants.Game.RACK_SIZE;

@Service(value = "scoreService")
@Slf4j
class ScoreServiceImpl implements ScoreService {

    @Override
    public Integer calculateConstructedWordScore(final ConstructedWord constructedWord) {
        final Integer baseScore = constructedWord.getCells().stream().mapToInt(this::calculateCellScore).sum();

        // calculate the cumulative word multiplier
        final Integer wordScoreMultiplier = constructedWord.getCells()
                .stream()
                .mapToInt(this::calculateCellMultiplier)
                .reduce(1, Math::multiplyExact);

        return baseScore * wordScoreMultiplier;
    }

    @Override
    public List<Bonus> calculateBonuses(final List<ConstructedWord> constructedWords, final VirtualRack virtualRack) {
        final List<Bonus> bonuses = new ArrayList<>();

        // a bingo score could only be added if there is a single word created by all rack tiles
        if (constructedWords.size() == 1 && virtualRack.getTiles().size() == RACK_SIZE
                && virtualRack.getTiles().stream().allMatch(VirtualTile::isSealed)) {
            log.debug("Bonus score {} has been added for {}", BINGO_SCORE, ActionType.BONUS_BINGO);
            bonuses.add(Bonus.builder().actionType(ActionType.BONUS_BINGO).score(BINGO_SCORE).build());
        }

        return bonuses;
    }

    /**
     * Calculate the score of a single {@link VirtualCell cell}
     * 
     * @param cell
     * @return the cell score
     */
    private Integer calculateCellScore(final VirtualCell cell) {
        // do not apply the multiplier if the new word extends an existing word which made use of it before
        return cell.isLastPlayed() ? cell.getLetterValueMultiplier() * cell.getValue() : cell.getValue();
    }

    /**
     * Calculate the multiplier of a single {@link VirtualCell cell}
     * 
     * @param cell
     * @return
     */
    private Integer calculateCellMultiplier(final VirtualCell cell) {
        // do not apply the multiplier if the new word extends an existing word which made use of it before
        return cell.isLastPlayed() ? cell.getWordScoreMultiplier() : 1;
    }

}
