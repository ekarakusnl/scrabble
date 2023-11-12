package com.gamecity.scrabble.model;

import java.io.Serializable;

import com.gamecity.scrabble.entity.Cell;
import com.gamecity.scrabble.entity.Game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Virtual representation of a {@link Cell cell} of {@link Board board} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = false)
public class VirtualCell implements Serializable {

    private static final long serialVersionUID = -1098326186343126874L;

    private Integer cellNumber;

    private Integer rowNumber;

    private Integer columnNumber;

    private String color;

    @Default
    private Integer letterValueMultiplier = 1;

    @Default
    private Integer wordScoreMultiplier = 1;

    private boolean hasRight;

    private boolean hasLeft;

    private boolean hasTop;

    private boolean hasBottom;

    private boolean center;

    private String letter;

    @Default
    private Integer value = 0;

    private boolean sealed;

    private Integer roundNumber;

    private boolean lastPlayed;

}
