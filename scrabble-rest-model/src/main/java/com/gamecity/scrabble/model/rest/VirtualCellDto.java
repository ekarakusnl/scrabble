package com.gamecity.scrabble.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a cell on a {@link VirtualBoardDto board}
 * 
 * @author ekarakus
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class VirtualCellDto {

    @JsonProperty("cellNumber")
    private Integer cellNumber;

    @JsonProperty("rowNumber")
    private Integer rowNumber;

    @JsonProperty("columnNumber")
    private Integer columnNumber;

    @JsonProperty("color")
    private String color;

    @JsonProperty("letterValueMultiplier")
    private Integer letterValueMultiplier;

    @JsonProperty("wordScoreMultiplier")
    private Integer wordScoreMultiplier;

    @JsonProperty("hasRight")
    private boolean hasRight;

    @JsonProperty("hasLeft")
    private boolean hasLeft;

    @JsonProperty("hasTop")
    private boolean hasTop;

    @JsonProperty("hasBottom")
    private boolean hasBottom;

    @JsonProperty("center")
    private boolean center;

    @JsonProperty("letter")
    private String letter;

    @JsonProperty("value")
    private Integer value;

    @JsonProperty("sealed")
    private boolean sealed;

    @JsonProperty("roundNumber")
    private Integer roundNumber;

    @JsonProperty("lastPlayed")
    private boolean lastPlayed;

}
