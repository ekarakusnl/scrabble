package com.gamecity.scrabble.model;

import java.io.Serializable;

import com.gamecity.scrabble.entity.Game;
import com.gamecity.scrabble.entity.Tile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Virtual representation of a {@link Tile tile} in a {@link VirtualRack rack} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = false)
public class VirtualTile implements Serializable {

    private static final long serialVersionUID = -67613320612860633L;

    private Integer number;

    private Integer rowNumber;

    private Integer columnNumber;

    private String letter;

    private Integer value;

    private boolean vowel;

    private Integer playerNumber;

    private Integer roundNumber;

    private boolean sealed;

}
