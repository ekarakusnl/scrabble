package com.gamecity.scrabble.model;

import com.gamecity.scrabble.entity.ActionType;
import com.gamecity.scrabble.entity.Player;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A bonus is an extra score for a {@link Player player} that is rewarded in a special condition
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class Bonus {

    private ActionType actionType;
    private Integer score;

}
