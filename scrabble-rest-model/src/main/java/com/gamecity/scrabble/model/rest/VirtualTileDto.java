package com.gamecity.scrabble.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a tile on a {@link VirtualRackDto rack}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class VirtualTileDto extends AbstractDto {

    @JsonProperty("playerNumber")
    private Integer playerNumber;

    @JsonProperty("number")
    private Integer number;

    @JsonProperty("rowNumber")
    private Integer rowNumber;

    @JsonProperty("columnNumber")
    private Integer columnNumber;

    @JsonProperty("letter")
    private String letter;

    @JsonProperty("value")
    private Integer value;

    @JsonProperty("vowel")
    private boolean vowel;

    @JsonProperty("roundNumber")
    private Integer roundNumber;

    @JsonProperty("sealed")
    private boolean sealed;

    @JsonProperty("exchanged")
    private boolean exchanged;

}
