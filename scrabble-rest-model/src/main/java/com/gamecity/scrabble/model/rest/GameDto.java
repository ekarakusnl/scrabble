package com.gamecity.scrabble.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a game
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class GameDto extends AbstractDto {

    @JsonProperty("ownerId")
    private Long ownerId;

    @JsonProperty("language")
    private String language;

    @JsonProperty("name")
    private String name;

    @JsonProperty("expectedPlayerCount")
    private Integer expectedPlayerCount;

    @JsonProperty("activePlayerCount")
    private Integer activePlayerCount;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    private String type;

    @JsonProperty("currentPlayerNumber")
    private Integer currentPlayerNumber;

    @JsonProperty("roundNumber")
    private Integer roundNumber;

    @JsonProperty("remainingTileCount")
    private Integer remainingTileCount;

    @JsonProperty("version")
    private Integer version;

}
