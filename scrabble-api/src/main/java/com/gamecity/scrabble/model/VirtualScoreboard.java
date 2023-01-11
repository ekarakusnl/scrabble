package com.gamecity.scrabble.model;

import java.io.Serializable;
import java.util.List;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Player;
import com.gamecity.scrabble.model.rest.PlayerDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Virtual representation of a score board consisting of {@link Player player} information such as
 * <code>score</code>, <code>name</code>, <code>statistics</code> etc. in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualScoreboard implements Serializable {

    private static final long serialVersionUID = 5525108209446087286L;

    private List<PlayerDto> players;

}
