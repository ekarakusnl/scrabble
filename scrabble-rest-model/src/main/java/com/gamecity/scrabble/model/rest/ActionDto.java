package com.gamecity.scrabble.model.rest;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents an action happened in the game
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class ActionDto extends AbstractDto implements Serializable {

    private static final long serialVersionUID = 6976452273839025780L;

    @JsonProperty("gameId")
    private Long gameId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("counter")
    private Integer counter;

    @JsonProperty("currentPlayerNumber")
    private Integer currentPlayerNumber;

    @JsonProperty("roundNumber")
    private Integer roundNumber;

    @JsonProperty("type")
    private String type;

    @JsonProperty("status")
    private String status;

}
